package com.smartroom.allocation.repository;

import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.BookingStatus;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Count conflicting bookings for a room within a time range
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room = :room " +
            "AND b.status = 'CONFIRMED' " +
            "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    Long countConflictingBookings(@Param("room") Room room,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    // Find bookings by user
    List<Booking> findByUser(User user);

    //Count bookings by user
    int countByUser(User user);

    // Find current bookings (ongoing at the given time)
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
            "AND b.startTime <= :currentTime AND b.endTime >= :currentTime")
    List<Booking> findCurrentBookings(@Param("currentTime") LocalDateTime currentTime);

    // Find upcoming bookings for a specific room
    @Query("SELECT b FROM Booking b WHERE b.room = :room " +
            "AND b.status = 'CONFIRMED' AND b.startTime > :currentTime")
    List<Booking> findUpcomingBookingsForRoom(@Param("room") Room room,
                                              @Param("currentTime") LocalDateTime currentTime);

    // Find bookings by status and updatedAt before a threshold
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.updatedAt < :threshold")
    List<Booking> findByStatusAndUpdatedAtBefore(@Param("status") BookingStatus status,
                                                 @Param("threshold") LocalDateTime threshold);
}