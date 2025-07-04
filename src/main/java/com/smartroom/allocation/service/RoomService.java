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

    /**
     * Update room details
     * @param id Room ID
     * @param roomUpdate Room details to update
     * @return Updated room
     */
    public Room updateRoom(Long id, Room roomUpdate) {
        Optional<Room> roomOpt = roomRepository.findById(id);
        if (!roomOpt.isPresent()) {
            throw new IllegalArgumentException("Room not found with id: " + id);
        }

        Room existingRoom = roomOpt.get();
        // Check if room number is being updated and if it already exists
        if (!existingRoom.getRoomNumber().equals(roomUpdate.getRoomNumber()) &&
                roomRepository.existsByRoomNumber(roomUpdate.getRoomNumber())) {
            throw new IllegalArgumentException("Room number already exists");
        }

        existingRoom.setRoomNumber(roomUpdate.getRoomNumber());
        existingRoom.setName(roomUpdate.getName());
        existingRoom.setCapacity(roomUpdate.getCapacity());
        existingRoom.setBuilding(roomUpdate.getBuilding());
        existingRoom.setFloor(roomUpdate.getFloor());
        existingRoom.setLocation(roomUpdate.getLocation());
        existingRoom.setRoomType(roomUpdate.getRoomType());
        existingRoom.setStatus(roomUpdate.getStatus());
        existingRoom.setActive(roomUpdate.isActive());

        return roomRepository.save(existingRoom);
    }

    /**
     * Delete room by ID
     * @param id Room ID
     * @return true if deleted, false if not found
     */
    public boolean deleteRoomById(Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Delete room by room number
     * @param roomNumber Room number
     * @return true if deleted, false if not found
     */
    public boolean deleteRoomByRoomNumber(String roomNumber) {
        Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
        if (roomOpt.isPresent()) {
            roomRepository.deleteById(roomOpt.get().getId());
            return true;
        }
        return false;
    }
}