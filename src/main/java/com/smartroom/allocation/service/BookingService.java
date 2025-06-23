package com.smartroom.allocation.service;

import com.smartroom.allocation.dto.RecurringBookingRequest;
import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.BookingStatus;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.repository.BookingRepository;
import com.smartroom.allocation.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository; //ADDED dependency

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a new single booking (non-recurring).
     * @param booking Booking to create
     * @return Created booking
     * @throws IllegalArgumentException if there's a conflict or invalid booking details
     */
    public Booking createBooking(Booking booking) {
        // Check for booking conflicts
        Long conflicts = bookingRepository.countConflictingBookings(
                booking.getRoom(),
                booking.getStartTime(),
                booking.getEndTime()
        );

        if (conflicts > 0) {
            throw new IllegalArgumentException("Room is already booked for the specified time");
        }

        // Validate booking time (must be in the future)
        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book rooms in the past");
        }

        if (booking.getEndTime().isBefore(booking.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Set Booking status to CONFIRMED
        booking.setStatus(BookingStatus.CONFIRMED);

        // Save the booking
        Booking savedBooking = bookingRepository.save(booking);

        // Update user points for gamification (5 points per booking)
        userService.updateUserPoints(booking.getUser().getId(), 5);

        // Send notification
        notificationService.sendBookingConfirmation(savedBooking);

        return savedBooking;
    }

    /*
     * ADDED: Creates a series of recurring bookings based on a request, with a specified weekly interval.
     * @param request The recurring booking details, including the interval.\
     * @param user The user making the booking.
     * @ return A list of the created booking objects.
     */
    @Transactional
    public List<Booking> createRecurringBookings(RecurringBookingRequest request, User user) {
        Room room = roomRepository.findByRoomNumber(request.getRoomNumber())
                .orElseThrow(() -> new IllegalArgumentException("Room with number " + request.getRoomNumber() + " not found."));

        List<Booking> newBookings = new ArrayList<>();
        String recurringGroupId = UUID.randomUUID().toString();

        int interval = (request.getIntervalWeeks() > 0) ? request.getIntervalWeeks() : 1;

        LocalDate currentDate = request.getSemesterStartDate();
        // Find the first valid day of the week on or after the start date
        while (currentDate.getDayOfWeek() != request.getDayOfWeek()) {
            currentDate = currentDate.plusDays(1);
        }

        // Loop from the first valid day, jumping by the specified week interval
        while (!currentDate.isAfter(request.getSemesterEndDate())) {
            LocalDateTime startTime = LocalDateTime.of(currentDate, request.getStartTime());
            LocalDateTime endTime = LocalDateTime.of(currentDate, request.getEndTime());

            // Check for conflicts for this specific instance
            Long conflicts = bookingRepository.countConflictingBookings(room, startTime, endTime);
            if (conflicts > 0) {
                throw new IllegalArgumentException("A conflict was found for the booking on " + currentDate +
                        ". The entire recurring booking series has been cancelled to ensure consistency.");
            }

            // Create a new booking instance
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setRoom(room);
            booking.setStartTime(startTime);
            booking.setEndTime(endTime);
            booking.setPurpose(request.getPurpose());
            booking.setNotes(request.getNotes());
            booking.setRecurring(true);
            booking.setRecurringGroupId(recurringGroupId);
            booking.setStatus(BookingStatus.CONFIRMED);

            newBookings.add(booking);

            // Jump to the next occurrence
            currentDate = currentDate.plusWeeks(interval);
        }

        if (newBookings.isEmpty()) {
            throw new IllegalArgumentException("No dates matching your criteria were found within the specified semester range.");
        }
        //save all booking instances to the database in as single transaction
        List<Booking> savedBookings = bookingRepository.saveAll(newBookings);

        //Update user points based on the number of bookings created
        userService.updateUserPoints(user.getId(), savedBookings.size() * 5);

        //  MODIFIED: Call the new summary notification method once for the entire series
        notificationService.sendRecurringBookingConfirmationSummary(savedBookings);

        return savedBookings;
    }

    /**
     * Cancel a booking
     * @param bookingId Booking ID to cancel
     * @param userId User requesting cancellation
     * @return Updated booking
     * @throws RuntimeException if booking or user not found, or permission denied
     */
    public Booking cancelBooking(Long bookingId, Long userId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (!bookingOpt.isPresent()) {
            throw new RuntimeException("Booking not found");
        }

        Booking booking = bookingOpt.get();

        // Check if user owns this booking or is admin
        User user = userService.findById(userId).orElseThrow(() ->
                new RuntimeException("User not found"));

        if (!booking.getUser().getId().equals(userId) &&
                !user.getRole().toString().equals("ADMIN")) {
            throw new RuntimeException("You don't have permission to cancel this booking");
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        Booking cancelledBooking = bookingRepository.save(booking);

        // Send notification about cancellation
        notificationService.sendCancellationNotification(cancelledBooking);

        return cancelledBooking;
    }

    /**
     * Get all bookings for a user
     * @param user User to get bookings for
     * @return List of user's bookings
     */
    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUser(user);
    }

    /**
     * Get current bookings (happening now)
     * @return List of current bookings
     */
    public List<Booking> getCurrentBookings() {
        return bookingRepository.findCurrentBookings(LocalDateTime.now());
    }

    /**
     * Get upcoming bookings for a room
     * @param room Room to check
     * @return List of upcoming bookings
     */
    public List<Booking> getUpcomingBookingsForRoom(Room room) {
        return bookingRepository.findUpcomingBookingsForRoom(room, LocalDateTime.now());
    }
}