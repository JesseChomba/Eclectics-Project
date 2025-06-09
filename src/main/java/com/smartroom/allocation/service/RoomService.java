package com.smartroom.allocation.service;

import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.RoomStatus;
import com.smartroom.allocation.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    /**
     * Get all active rooms
     * @return List of active rooms
     */
    public List<Room> getAllActiveRooms() {
        return roomRepository.findByActiveTrue();
    }

    /**
     * Find room by ID
     * @param id Room ID
     * @return Room if found
     */
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    /**
     * Find room by room number
     * @param roomNumber Room number to search for
     * @return Room if found
     */
    public Optional<Room> findByRoomNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }

    /**
     * Find available rooms for a specific time period
     * @param startTime Start time of booking
     * @param endTime End time of booking
     * @return List of available rooms
     */
    public List<Room> findAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
        return roomRepository.findAvailableRooms(startTime, endTime);
    }

    /**
     * Find rooms with minimum capacity
     * @param minCapacity Minimum capacity required
     * @return List of rooms with sufficient capacity
     */
    public List<Room> findRoomsByMinCapacity(int minCapacity) {
        return roomRepository.findByCapacityGreaterThanEqual(minCapacity);
    }

    /**
     * Update room status
     * @param roomId Room ID
     * @param status New status
     */
    public void updateRoomStatus(Long roomId, RoomStatus status) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.setStatus(status);
            roomRepository.save(room);
        }
    }

    /**
     * Create a new room
     * @param room Room to create
     * @return Created room
     */
    public Room createRoom(Room room) {
//        return roomRepository.save(room); // old implementation...did not check if unique iDs already exist
        if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new IllegalArgumentException("Room number already exists");
        }
        room.setActive(true); // Ensure new rooms are active by default
        return roomRepository.save(room);
    }
}