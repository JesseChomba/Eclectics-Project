package com.smartroom.allocation.controller;

import com.smartroom.allocation.dto.EquipmentResponseDTO;
import com.smartroom.allocation.entity.Equipment;
import com.smartroom.allocation.entity.EquipmentType;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.repository.BookingRepository;
import com.smartroom.allocation.repository.EquipmentRepository;
import com.smartroom.allocation.service.NotificationService;
import com.smartroom.allocation.service.RoomService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "*")
public class EquipmentController {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentController.class);

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationService notificationService;

    /*Helper method to notify users with active bookings that
     * the equipment in their rooms has changed */

    private void notifyUsersForRoom(Room room, String equipmentName, String action){
        List<String> recipientEmails =bookingRepository.findByRoomAndEndTimeAfter(room, LocalDateTime.now())
                .stream()
                .map(booking -> booking.getUser().getEmail())
                .distinct()
                .collect(Collectors.toList());
        if(!recipientEmails.isEmpty()){
            String subject = "Equipment Update Notification for Room"+ room.getRoomNumber();
            String message = String.format("The Equipment '%s' in Room %s has been %s", equipmentName,room.getRoomNumber(),action);
            notificationService.sendEquipmentUpdateNotification(recipientEmails,subject,message);
            logger.info("Notifications queued for {} users for room {}\n",recipientEmails.size(),room.getRoomNumber());

        }
    }

    /*
     * Get all the Equipment
     * */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEquipment(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<Equipment> equipment = equipmentRepository.findAll();
            List<EquipmentResponseDTO> equipmentDTOs = equipment.stream()
                    .map(EquipmentResponseDTO::new)
                    .collect(Collectors.toList());
            response.put("Status", 1);
            response.put("Message", "Equipment retrieved successfully");
            response.put("Data", equipmentDTOs);
            // response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to retrieve equipment: {}", e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve equipment: " + e.getMessage());
            response.put("Data", "");
            // response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /*
     * Get Equipment via id
     * */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEquipmentById(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //  response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<Equipment> equipmentOpt = equipmentRepository.findById(id);
            if (!equipmentOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Equipment not found with id: " + id);
                response.put("Data", "");
                //   response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("Status", 1);
            response.put("Message", "Equipment retrieved successfully");
            response.put("Data", new EquipmentResponseDTO(equipmentOpt.get()));
            // response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to retrieve equipment with id {}: {}", id, e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve equipment: " + e.getMessage());
            response.put("Data", "");
            // response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /*
     * Get Working equipment
     * */
    @GetMapping("/working")
    public ResponseEntity<Map<String, Object>> getWorkingEquipment(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //   response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<Equipment> equipment = equipmentRepository.findByWorkingTrue();
            List<EquipmentResponseDTO> equipmentDTOs = equipment.stream()
                    .map(EquipmentResponseDTO::new)
                    .collect(Collectors.toList());
            response.put("Status", 1);
            response.put("Message", "Working equipment retrieved successfully");
            response.put("Data", equipmentDTOs);
            // response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to retrieve working equipment: {}", e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve working equipment: " + e.getMessage());
            response.put("Data", "");
            //   response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /*
     * Get Equipment for a given room number
     * */
    @GetMapping("/room/{roomNumber}")
    public ResponseEntity<Map<String, Object>> getEquipmentByRoom(@PathVariable String roomNumber, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //  response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<Room> roomOpt = roomService.findByRoomNumber(roomNumber);
            if (!roomOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Room not found with roomNumber: " + roomNumber);
                response.put("Data", "");
                //  response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            List<Equipment> equipment = equipmentRepository.findByRoom(roomOpt.get());
            List<EquipmentResponseDTO> equipmentDTOs = equipment.stream()
                    .map(EquipmentResponseDTO::new)
                    .collect(Collectors.toList());
            response.put("Status", 1);
            response.put("Message", "Equipment for room " + roomNumber + " retrieved successfully");
            response.put("Data", equipmentDTOs);
            //  response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to retrieve equipment for room {}: {}", roomNumber, e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve equipment: " + e.getMessage());
            response.put("Data", "");
            //   response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /*
     * Get equipment via type
     * */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getEquipmentByType(@PathVariable String type, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //   response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            EquipmentType equipmentType;
            try {
                equipmentType = EquipmentType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                response.put("Status", 0);
                response.put("Message", "Invalid equipment type: " + type);
                response.put("Data", "");
                //  response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            List<Equipment> equipmentList = equipmentRepository.findByType(equipmentType);
            List<EquipmentResponseDTO> equipmentDTOs = equipmentList.stream()
                    .map(EquipmentResponseDTO::new)
                    .collect(Collectors.toList());
            response.put("Status", 1);
            response.put("Message", "Equipment of type " + type + " retrieved successfully");
            response.put("Data", equipmentDTOs);
            //   response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to retrieve equipment by type {}: {}", type, e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve equipment by type: " + e.getMessage());
            response.put("Data", "");
            //   response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /* Create an equipment --requires admin privileges
     * */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createEquipment(@Valid @RequestBody Equipment equipment, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //  response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (equipmentRepository.findByName(equipment.getName()).stream()
                    .anyMatch(e -> e.getRoom() != null && e.getRoom().equals(equipment.getRoom()))) {
                response.put("Status", 0);
                response.put("Message", "Equipment with name " + equipment.getName() + " already exists in this room");
                response.put("Data", "");
                //   response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            if (equipment.getRoom() != null) {
                Optional<Room> roomOpt = roomService.findById(equipment.getRoom().getId());
                if (!roomOpt.isPresent()) {
                    response.put("Status", 0);
                    response.put("Message", "Room not found with id: " + equipment.getRoom().getId());
                    response.put("Data", "");
                    //   response.put("Token", "");
                    return ResponseEntity.badRequest().body(response);
                }
                equipment.setRoom(roomOpt.get());
            }

            Equipment createdEquipment = equipmentRepository.save(equipment);
            response.put("Status", 1);
            response.put("Message", "Equipment created successfully");
            response.put("Data", new EquipmentResponseDTO(createdEquipment));
            //  response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to create equipment: {}", e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to create equipment: " + e.getMessage());
            response.put("Data", "");
            //  response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Create new equipment and assign to a room by room number (Admin only)
     * @param equipment Equipment details
     * @param roomNumber Room number to assign equipment to
     * @return Created equipment in standardized format
     */
    @PostMapping("/room/{roomNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createEquipment(@Valid @RequestBody Equipment equipment,
                                                               @PathVariable String roomNumber) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Room> roomOpt = roomService.findByRoomNumber(roomNumber);
            if (!roomOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Room not found with number: " + roomNumber);
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            equipment.setRoom(roomOpt.get());
            Equipment createdEquipment = equipmentRepository.save(equipment);

            response.put("Status", 1);
            response.put("Message", "Equipment created successfully");
            response.put("Data", createdEquipment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to create equipment: " + e.getMessage());
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /*Update Equipment details via id. Requires admin privileges*/
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateEquipment(@PathVariable Long id, @Valid @RequestBody Equipment equipmentUpdate, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<Equipment> equipmentOpt = equipmentRepository.findById(id);
            if (!equipmentOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Equipment not found with id: " + id);
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            Equipment equipment = equipmentOpt.get();
            Room oldRoom= equipment.getRoom();

            // Conditionally update fields to prevent nulling out existing data
            if (equipmentUpdate.getName() != null) {
                equipment.setName(equipmentUpdate.getName());
            }
            if (equipmentUpdate.getType() != null) {
                equipment.setType(equipmentUpdate.getType());
            }
            if (equipmentUpdate.getDescription() != null) {
                equipment.setDescription(equipmentUpdate.getDescription());
            }
            // For 'working' status, check if it's explicitly provided in the update
            // Now that 'working' is a Boolean wrapper, it can be null if not present in JSON.
            if (equipmentUpdate.getWorking() != null) {
                equipment.setWorking(equipmentUpdate.getWorking());
            }


            if (equipmentUpdate.getRoom() != null) {
                Optional<Room> roomOpt = roomService.findById(equipmentUpdate.getRoom().getId());
                if (!roomOpt.isPresent()) {
                    response.put("Status", 0);
                    response.put("Message", "Room not found with id: " + equipmentUpdate.getRoom().getId());
                    response.put("Data", "");
                    return ResponseEntity.badRequest().body(response);
                }
                equipment.setRoom(roomOpt.get());
            } else {
                // If room is explicitly set to null in the request, unassign it
                // Or if it was simply omitted from the JSON, and you want to unassign it
                // This behavior depends on your API design. If omitting means "don't change",
                // then this else block might need a more nuanced check (e.g., a separate DTO
                // that distinguishes between 'null' and 'not present').
                // For now, assuming if room is null in equipmentUpdate, it means unassign.
                equipment.setRoom(null);
            }

            Equipment updatedEquipment = equipmentRepository.save(equipment);

            // Notify users if the equipment is still assigned to a room or was previously assigned
            if(updatedEquipment.getRoom() !=null){
                notifyUsersForRoom(updatedEquipment.getRoom(), updatedEquipment.getName(), "updated");
            } else if (oldRoom !=null) {
                notifyUsersForRoom(oldRoom, updatedEquipment.getName(),"updated and unassigned");
            }
            response.put("Status", 1);
            response.put("Message", "Equipment updated successfully");
            response.put("Data", new EquipmentResponseDTO(updatedEquipment));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update equipment with id {}: {}", id, e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to update equipment: " + e.getMessage());
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update equipment details (Admin only)
     * @param equipmentId Equipment ID to update
     * @param roomNumber Room number to assign equipment to
     * @param equipmentUpdate Updated equipment details (partial update allowed)
     * @return Updated equipment in standardized format
     */
    @PutMapping("/{equipmentId}/room/{roomNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateEquipment(@PathVariable Long equipmentId,
                                                               @PathVariable String roomNumber,
                                                               @Valid @RequestBody Equipment equipmentUpdate) { // Renamed to equipmentUpdate for clarity
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Equipment> existingEquipmentOpt = equipmentRepository.findById(equipmentId);
            if (!existingEquipmentOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Equipment not found with id: " + equipmentId);
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Room> roomOpt = roomService.findByRoomNumber(roomNumber);
            if (!roomOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Room not found with number: " + roomNumber);
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            Equipment existingEquipment = existingEquipmentOpt.get();
            Room oldRoom = existingEquipment.getRoom(); // Store old room for notification

            // Conditionally update fields from equipmentUpdate
            if (equipmentUpdate.getName() != null) {
                existingEquipment.setName(equipmentUpdate.getName());
            }
            if (equipmentUpdate.getType() != null) {
                existingEquipment.setType(equipmentUpdate.getType());
            }
            if (equipmentUpdate.getDescription() != null) {
                existingEquipment.setDescription(equipmentUpdate.getDescription());
            }
            // Now that 'working' is a Boolean wrapper, it can be null if not present in JSON.
            // This allows us to preserve the existing value if the field is omitted.
            if (equipmentUpdate.getWorking() != null) {
                existingEquipment.setWorking(equipmentUpdate.getWorking());
            }

            // Assign the room from the path variable
            existingEquipment.setRoom(roomOpt.get());

            Equipment updatedEquipment = equipmentRepository.save(existingEquipment);

            // Notify users if the equipment's room changed or its details were updated
            if (updatedEquipment.getRoom() != null && !updatedEquipment.getRoom().equals(oldRoom)) {
                notifyUsersForRoom(updatedEquipment.getRoom(), updatedEquipment.getName(), "moved to this room");
                if (oldRoom != null) {
                    notifyUsersForRoom(oldRoom, updatedEquipment.getName(), "moved from this room");
                }
            } else if (updatedEquipment.getRoom() != null) {
                notifyUsersForRoom(updatedEquipment.getRoom(), updatedEquipment.getName(), "updated");
            } else if (oldRoom != null) {
                // If it was unassigned (e.g., if the other update endpoint was used to set room to null)
                notifyUsersForRoom(oldRoom, updatedEquipment.getName(), "unassigned");
            }


            response.put("Status", 1);
            response.put("Message", "Equipment updated successfully");
            response.put("Data", new EquipmentResponseDTO(updatedEquipment));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update equipment with id {}: {}", equipmentId, e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to update equipment: " + e.getMessage());
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /*
     * Unassign equipment to a particular room*/
    @PutMapping("/{id}/unassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> unassignEquipment(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //  response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<Equipment> equipmentOpt = equipmentRepository.findById(id);
            if (!equipmentOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Equipment not found with id: " + id);
                response.put("Data", "");
                //   response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            Equipment equipment = equipmentOpt.get();
            Room oldRoom=equipment.getRoom();
            equipment.setRoom(null);
            Equipment updatedEquipment = equipmentRepository.save(equipment);

            //notify users of the room from which the equipment was unassigned
            if (oldRoom!=null){
                notifyUsersForRoom(oldRoom, equipment.getName(), "unassigned");
            }

            response.put("Status", 1);
            response.put("Message", "Equipment unassigned successfully");
            response.put("Data", new EquipmentResponseDTO(updatedEquipment));
            //  response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to unassign equipment with id {}: {}", id, e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to unassign equipment: " + e.getMessage());
            response.put("Data", "");
            //  response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional  //Added to manage the transaction
    public ResponseEntity<Map<String, Object>> deleteEquipment(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //     response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<Equipment> equipmentOpt = equipmentRepository.findById(id);
            if (!equipmentOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Equipment not found with id: " + id);
                response.put("Data", "");
                //    response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            Equipment equipment = equipmentOpt.get();
            Room oldRoom = equipment.getRoom();

            //notify users before deletion of the equipment
            if(oldRoom !=null) {
                notifyUsersForRoom(oldRoom, equipment.getName(), "deleted");

                //***Fix for bug causing it not to delete equipment by its ID
                //Explicitly remove the equipment from the room's collection to -
                //update parent side of the relationship.
                oldRoom.getEquipment().remove(equipment);

            }
            //now we delete the equipment entity itself
            equipmentRepository.deleteById(id);
            response.put("Status", 1);
            response.put("Message", "Equipment deleted successfully");
            response.put("Data", "");
            response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to delete equipment with id {}: {}", id, e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to delete equipment: " + e.getMessage());
            response.put("Data", "");
            response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/room/{roomNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteEquipmentByRoom(@PathVariable String roomNumber, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<Room> roomOpt = roomService.findByRoomNumber(roomNumber);
            if (!roomOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Room not found with roomNumber: " + roomNumber);
                response.put("Data", "");
                response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            Room room=roomOpt.get();
            //List<Equipment> equipmentList = equipmentRepository.findByRoom(roomOpt.get());
            List <Equipment> equipmentList=new ArrayList<>(room.getEquipment());
            if (equipmentList.isEmpty()) {
                response.put("Status", 1); //return success as there is nothing to delete
                response.put("Message", "No equipment found for room: " + roomNumber);
                response.put("Data", "");
                response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            // Notify users before deletion
            String equipmentNames = equipmentList.stream()
                    .map(Equipment::getName)
                    .collect(Collectors.joining(", "));
            notifyUsersForRoom(room,equipmentNames,"deleted");

            // *** THE FIX ***
            // 1. Explicitly sever the relationship from the parent side.
            room.getEquipment().clear();

            //2. Delete the now orphaned equipment entities.
            equipmentRepository.deleteAll(equipmentList);
            response.put("Status", 1);
            response.put("Message", "All equipment for room " + roomNumber + " deleted successfully");
            response.put("Data", "");
            response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to delete equipment for room {}: {}", roomNumber, e.getMessage());
            response.put("Status", 0);
            response.put("Message", "Failed to delete equipment for room: " + e.getMessage());
            response.put("Data", "");
            response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
