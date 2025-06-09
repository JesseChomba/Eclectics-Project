package com.smartroom.allocation.service;

import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.BookingStatus;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a new booking
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

        // Save the booking
        Booking savedBooking = bookingRepository.save(booking);

        // Update user points for gamification (5 points per booking)
        userService.updateUserPoints(booking.getUser().getId(), 5);

        // Send notification
        notificationService.sendBookingConfirmation(savedBooking);

        return savedBooking;
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