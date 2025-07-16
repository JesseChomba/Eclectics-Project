package com.smartroom.allocation.service;

import com.smartroom.allocation.dto.RoomResponseDTO;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.RoomStatus;
import com.smartroom.allocation.repository.BookingRepository;
import com.smartroom.allocation.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired NotificationService notificationService;

    @Autowired BookingRepository bookingRepository;

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
     * Only updates non-null fields from the roomUpdate object to
     * prevent accidentally nullifying existing data
     * @param id Room ID
     * @param roomUpdate Room details to update
     * @return Updated room
     * @throws IllegalArgumentException if room not found or number already exists
     */
    public Room updateRoom(Long id, Room roomUpdate) {
        Optional<Room> roomOpt = roomRepository.findById(id);
        if (!roomOpt.isPresent()) {
            throw new IllegalArgumentException("Room not found with id: " + id);
        }

        Room existingRoom = roomOpt.get();

        // Conditionally update roomNumber, checking for uniqueness only if it's being changed
        if (roomUpdate.getRoomNumber() != null && !existingRoom.getRoomNumber().equals(roomUpdate.getRoomNumber())) {
            if (roomRepository.existsByRoomNumber(roomUpdate.getRoomNumber())) {
                throw new IllegalArgumentException("Room number already exists: " + roomUpdate.getRoomNumber());
            }
            existingRoom.setRoomNumber(roomUpdate.getRoomNumber());
        }

        // Conditionally update other fields
        if (roomUpdate.getName() != null) {
            existingRoom.setName(roomUpdate.getName());
        }
        // Now that 'capacity' is an Integer wrapper, it can be null if not present in JSON.
        if (roomUpdate.getCapacity() != null) {
            existingRoom.setCapacity(roomUpdate.getCapacity());
        }
        if (roomUpdate.getBuilding() != null) {
            existingRoom.setBuilding(roomUpdate.getBuilding());
        }
        if (roomUpdate.getFloor() != null) {
            existingRoom.setFloor(roomUpdate.getFloor());
        }
        if (roomUpdate.getLocation() != null) {
            existingRoom.setLocation(roomUpdate.getLocation());
        }
        if (roomUpdate.getRoomType() != null) {
            existingRoom.setRoomType(roomUpdate.getRoomType());
        }
        if (roomUpdate.getStatus() != null) {
            existingRoom.setStatus(roomUpdate.getStatus());
        }
        // Now that 'active' is a Boolean wrapper, it can be null if not present in JSON.
        if (roomUpdate.isActive() != null) {
            existingRoom.setActive(roomUpdate.isActive());
        }
        //return roomRepository.save(existingRoom);
        //notify users with upcoming bookings
        Room updatedRoom= roomRepository.save(existingRoom);
        // Notify users with upcoming bookings
        List<String> recipientEmails = bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getRoom().getId().equals(updatedRoom.getId()))
                .filter(booking -> booking.getEndTime().isAfter(LocalDateTime.now()))
                .map(booking -> booking.getUser().getEmail())
                .distinct()
                .collect(Collectors.toList());
        if (!recipientEmails.isEmpty()) {
            notificationService.sendRoomUpdateNotification(updatedRoom, recipientEmails);
        }

        return updatedRoom;
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