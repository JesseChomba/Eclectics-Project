package com.smartroom.allocation.service;

import com.smartroom.allocation.dto.BookingResponseDTO;
import com.smartroom.allocation.dto.BookingUpdateDTO;
import com.smartroom.allocation.dto.RecurringBookingRequest;
import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.BookingStatus;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.exception.ResourceNotFoundException;
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
        //UPDATED: Fetch room by ID instead of room number
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + request.getRoomId() + " not found."));

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
     * Update an existing booking
     * @param bookingId the ID of the booking to update.
     * @param updateDTO the DTo containing the updated booking details.
     * @param username The username of the currently authenticated user.
     * @return The updated booking.
     * @throws IllegalArgumentException if the boking is in the past or the new time slot is invlaid or unavailable.
     * @throws ResourceNotFoundException if the booking is not found.
     * @throws SecurityException if the user does not own the booking*/
    @Transactional
    public BookingResponseDTO updateBooking(Long bookingId, BookingUpdateDTO updateDTO, String username) { // Changed return type
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // 1. Check if the authenticated user is the owner of the booking
        if (!existingBooking.getUser().getUsername().equals(username)) {
            throw new SecurityException("You do not have permission to update this booking.");
        }

        // 2. Check if the booking is in the past or currently active
        if (existingBooking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot update a booking that has already started or is in the past.");
        }

        // Store old booking details for notification BEFORE potential updates
        Booking oldBooking = new Booking();
        oldBooking.setId(existingBooking.getId());
        oldBooking.setStartTime(existingBooking.getStartTime());
        oldBooking.setEndTime(existingBooking.getEndTime());
        oldBooking.setPurpose(existingBooking.getPurpose());
        oldBooking.setRoom(existingBooking.getRoom());
        oldBooking.setUser(existingBooking.getUser());

        LocalDateTime proposedStartTime = updateDTO.getStartTime();
        LocalDateTime proposedEndTime = updateDTO.getEndTime();
        String proposedPurpose = updateDTO.getPurpose();

        // Determine the effective new start and end times for validation and update
        LocalDateTime effectiveNewStartTime = (proposedStartTime != null) ? proposedStartTime : existingBooking.getStartTime();
        LocalDateTime effectiveNewEndTime = (proposedEndTime != null) ? proposedEndTime : existingBooking.getEndTime();

        // 3. Validate new time slot IF times are being changed or both are provided.
        if (effectiveNewStartTime.isAfter(effectiveNewEndTime) || effectiveNewStartTime.isEqual(effectiveNewEndTime)) {
            throw new IllegalArgumentException("New end time must be after new start time.");
        }

        // 4. Check for room availability for the new effective time slot, but ONLY if the times have actually changed
        boolean timesChanged = !effectiveNewStartTime.equals(existingBooking.getStartTime()) || !effectiveNewEndTime.equals(existingBooking.getEndTime());

        if (timesChanged) {
            List<Booking> overlappingBookings = bookingRepository.findOverlappingBookingsExcludingCurrent(
                    existingBooking.getRoom().getId(),
                    effectiveNewStartTime,
                    effectiveNewEndTime,
                    existingBooking.getId()
            );
            if (!overlappingBookings.isEmpty()) {
                throw new IllegalArgumentException("Room is not available during the new specified time.");
            }
            // Update the booking times only if they changed and are available
            existingBooking.setStartTime(effectiveNewStartTime);
            existingBooking.setEndTime(effectiveNewEndTime);
        }

        // 5. Update purpose if provided
        if (proposedPurpose != null) {
            existingBooking.setPurpose(proposedPurpose);
        }

        Booking updatedBooking = bookingRepository.save(existingBooking);

        // 6. Send notification email
        notificationService.sendBookingUpdatedEmail(oldBooking, updatedBooking);

        return new BookingResponseDTO(updatedBooking); // Return DTO
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
     * Fetches and updates the status of bookings that have ended.
     * This method is intended to be called by a scheduled task.
     */
    @Transactional
    public void updateCompletedBookingsStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookingsToComplete = bookingRepository.findConfirmedBookingsEndedBefore(now);

        for (Booking booking : bookingsToComplete) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
            // Optionally, you could send a notification here, but usually not needed for auto-completion
        }
        // Log the number of bookings updated
        // This logging is typically done in the scheduled task itself, but can be here too.
        if (!bookingsToComplete.isEmpty()) {
            System.out.printf("Updated %d bookings to COMPLETED status.%n", bookingsToComplete.size());
        }
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