package com.smartroom.allocation.controller;

import com.smartroom.allocation.dto.RoomResponseDTO;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.RoomStatus;
import com.smartroom.allocation.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomService roomService;

    /**
     * Get all active rooms
     * @return List of active rooms
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRooms(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 1);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            List<Room> rooms = roomService.getAllActiveRooms();
            List<RoomResponseDTO> roomDTOs = rooms.stream()
                    .map(RoomResponseDTO::new)
                    .collect(Collectors.toList());
            response.put("Status", 1);
            response.put("Message", "Rooms retrieved successfully");
            response.put("Data", roomDTOs);
            //response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve rooms: " + e.getMessage());
            response.put("Data", "");
            //response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get room by ID
     * @param id Room ID
     * @return Room details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoomById(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Room room = roomService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
            RoomResponseDTO roomDTO = new RoomResponseDTO(room);
            response.put("Status", 1);
            response.put("Message", "Room retrieved successfully");
            response.put("Data", roomDTO);
            // response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("Status", 0);
            response.put("Message", e.getMessage());
            response.put("Data", "");
            // response.put("Token", "");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve room: " + e.getMessage());
            response.put("Data", "");
            //response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get available rooms for a specific time period
     * @param startTime Start time (format: yyyy-MM-dd'T'HH:mm:ss)
     * @param endTime End time (format: yyyy-MM-dd'T'HH:mm:ss)
     * @return List of available rooms
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                // response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            if (startTime.isBefore(LocalDateTime.now())) {
                response.put("Status", 0);
                response.put("Message", "Start time cannot be in the past");
                response.put("Data", "");
                // response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            if (endTime.isBefore(startTime)) {
                response.put("Status", 0);
                response.put("Message", "End time must be after start time");
                response.put("Data", "");
                // response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            List<Room> rooms = roomService.findAvailableRooms(startTime, endTime);
            List<RoomResponseDTO> roomDTOs = rooms.stream()
                    .map(RoomResponseDTO::new)
                    .collect(Collectors.toList());
            response.put("Status", 1);
            response.put("Message", "Available rooms retrieved successfully");
            response.put("Data", roomDTOs);
            // response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve available rooms: " + e.getMessage());
            response.put("Data", "");
            // response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Search rooms by minimum capacity
     * @param minCapacity Minimum capacity required
     * @return List of rooms with sufficient capacity
     */
    @GetMapping("/capacity/{minCapacity}")
    public ResponseEntity<Map<String, Object>> getRoomsByMinCapacity(
            @PathVariable int minCapacity,
            Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //   response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            List<Room> rooms = roomService.findRoomsByMinCapacity(minCapacity);
            List<RoomResponseDTO> roomDTOs = rooms.stream()
                    .map(RoomResponseDTO::new)
                    .collect(Collectors.toList());
            response.put("Status", 1);
            response.put("Message", "Rooms with capacity >= " + minCapacity + " retrieved successfully");
            response.put("Data", roomDTOs);
            //  response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve rooms by capacity: " + e.getMessage());
            response.put("Data", "");
            //  response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Create a new room (Admin only)
     * @param room Room to create
     * @return Created room
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createRoom(@RequestBody Room room, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //  response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Room createdRoom = roomService.createRoom(room);
            RoomResponseDTO roomDTO = new RoomResponseDTO(createdRoom);
            response.put("Status", 1);
            response.put("Message", "Room created successfully");
            response.put("Data", roomDTO);
            //  response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("Status", 0);
            response.put("Message", e.getMessage()); // "Room number already exists"
            response.put("Data", "");
            //  response.put("Token", "");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to create room: " + e.getMessage());
            response.put("Data", "");
            //   response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateRoomStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //    response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            RoomStatus status = RoomStatus.valueOf(statusUpdate.get("status"));
            roomService.updateRoomStatus(id, status);
            Room updatedRoom = roomService.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
            response.put("Status", 1);
            response.put("Message", "Room status updated successfully");
            response.put("Data", new RoomResponseDTO(updatedRoom));
            //   response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to update room status: " + e.getMessage());
            response.put("Data", "");
            //  response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update room details (Admin only)
     * @param id Room ID
     * @param roomUpdate Room details to update
     * @return Updated room
     * */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String,Object>> updateRoom(@PathVariable Long id, @RequestBody Room roomUpdate,Authentication auth){
        Map<String,Object> response = new HashMap<>();
        try{
            if (auth == null || auth.getName() == null){
                response.put("Status",0);
                response.put("Message","Authentication required");
                response.put("Data", "");
                //    response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Room updatedRoom = roomService.updateRoom(id, roomUpdate);
            response.put("Status", 1);
            response.put("Message", "Room updated successfully");
            response.put("Data", new RoomResponseDTO(updatedRoom));
            // response.put("Token", "");
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e) {
            response.put("Status", 0);
            response.put("Message", e.getMessage());
            response.put("Data", "");
            //  response.put("Token", "");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to update room: " + e.getMessage());
            response.put("Data", "");
            // response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete room by ID (Admin only)
     * @param id Room ID
     * @return Success/Failure response
     * */

    /**
     * Delete room by ID (Admin only)
     * @param id Room ID
     * @return Success/failure response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteRoomById(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //    response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            boolean deleted = roomService.deleteRoomById(id);
            if (deleted) {
                response.put("Status", 1);
                response.put("Message", "Room deleted successfully");
                response.put("Data", "");
                //    response.put("Token", "");
                return ResponseEntity.ok(response);
            } else {
                response.put("Status", 0);
                response.put("Message", "Room not found with id: " + id);
                response.put("Data", "");
                //   response.put("Token", "");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to delete room:" + e.getMessage());
            response.put("Data", "");
            //  response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}