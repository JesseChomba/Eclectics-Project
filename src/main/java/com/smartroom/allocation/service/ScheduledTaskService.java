package com.smartroom.allocation.service;

import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.BookingStatus;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.RoomStatus;
import com.smartroom.allocation.repository.BookingRepository;
import com.smartroom.allocation.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingService bookingService;

    /**
     * Delete cancelled bookings older than 30 days.
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteOldCancelledBookings() {
        logger.info("Starting scheduled task to delete old cancelled bookings");
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        List<Booking> cancelledBookings = bookingRepository.findByStatusAndUpdatedAtBefore(
                BookingStatus.CANCELLED, threshold
        );

        if (!cancelledBookings.isEmpty()) {
            bookingRepository.deleteAll(cancelledBookings);
            logger.info("Deleted {} old cancelled bookings", cancelledBookings.size());
        } else {
            logger.info("No old cancelled bookings found to delete");
        }
    }

    /**
     * Update room statuses based on active bookings.
     * Runs every minute to check booking start/end times.
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void updateRoomStatuses() {
        logger.info("Starting scheduled task to update room statuses");
        LocalDateTime now = LocalDateTime.now();

        // Get all confirmed bookings active at the current time
        List<Booking> activeBookings = bookingRepository.findCurrentBookings(now);

        // Identify rooms that are currently occupied
        Set<Long> occupiedRoomIds = new HashSet<>();
        for (Booking booking : activeBookings) {
            occupiedRoomIds.add(booking.getRoom().getId());
        }

        // Get all rooms
        List<Room> rooms = roomRepository.findAll();

        // Update each room's status
        for (Room room : rooms) {
            RoomStatus newStatus = occupiedRoomIds.contains(room.getId())
                    ? RoomStatus.OCCUPIED
                    : RoomStatus.AVAILABLE;

            if (room.getStatus() != newStatus) {
                room.setStatus(newStatus);
                roomRepository.save(room);
                logger.info("Updated room {} (ID: {}) status to {}",
                        room.getRoomNumber(), room.getId(), newStatus);
            }
        }

        logger.info("Completed room status update task");
    }

    /**
     * Scheduled task to update booking statuses to COMPLETED.
     * Runs every 5 minutes (300000 milliseconds).
     * This calls the logic residing in BookingService.
     */
    @Scheduled(fixedRate = 300000) // Runs every 5 minutes
    // @Scheduled(cron = "0 0 * * * ?") // Example: Runs at the top of every hour
    public void updateCompletedBookingsScheduled() {
        logger.info("Starting scheduled task to update booking statuses to COMPLETED...");
        try {
            bookingService.updateCompletedBookingsStatus(); // Call the method in BookingService
            logger.info("Finished scheduled task to update booking statuses.");
        } catch (Exception e) {
            logger.error("Error during scheduled booking status update: {}", e.getMessage(), e);
        }
    }
}