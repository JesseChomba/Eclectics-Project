package com.smartroom.allocation.repository;

import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.RoomStatus;
import com.smartroom.allocation.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Find room by room number
    Optional<Room> findByRoomNumber(String roomNumber);

    // *** Add this method to check for existence of room number ***
    boolean existsByRoomNumber(String roomNumber);



    // Find all active rooms
    List<Room> findByActiveTrue();

    // Find rooms by status
    List<Room> findByStatus(RoomStatus status);

    // Find rooms by type
    List<Room> findByRoomType(RoomType roomType);

    // Find rooms by building
    List<Room> findByBuilding(String building);

    // Find rooms with minimum capacity
    List<Room> findByCapacityGreaterThanEqual(int minCapacity);

    // Find available rooms at a specific time (complex query)
    @Query("SELECT r FROM Room r WHERE r.active = true AND r.status = 'AVAILABLE' " +
            "AND r.id NOT IN (" +
            "SELECT b.room.id FROM Booking b WHERE b.status = 'CONFIRMED' " +
            "AND ((b.startTime <= :startTime AND b.endTime > :startTime) " +
            "OR (b.startTime < :endTime AND b.endTime >= :endTime) " +
            "OR (b.startTime >= :startTime AND b.endTime <= :endTime)))")
    List<Room> findAvailableRooms(@Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    //AdminDashboard: Count all rooms
    long count();

    //AdminDashboard: Count all rooms with status AVAILABLE
    long countByStatus(RoomStatus status);

    //AdminDashboard: Count all active rooms
    long countByActiveTrue();
}