package com.smartroom.allocation.controller;

import com.smartroom.allocation.entity.Equipment;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.repository.EquipmentRepository;
import com.smartroom.allocation.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "*")
public class EquipmentController {

    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private RoomService roomService;

    /**
     * Get all equipment
     * @return List of equipment
     */
    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        List<Equipment> equipment = equipmentRepository.findAll();
        return ResponseEntity.ok(equipment);
    }

    /**
     * Get all working equipment
     * @return List of working equipment
     */
    @GetMapping("/working")
    public ResponseEntity<List<Equipment>> getWorkingEquipment() {
        List<Equipment> workingEquipment = equipmentRepository.findByWorkingTrue();
        return ResponseEntity.ok(workingEquipment);
    }
    @GetMapping("/room/{roomNumber}")
    public ResponseEntity<List<Equipment>> getEquipmentByRoom(@PathVariable String roomNumber) {
        try {
            Room room = roomService.findByRoomNumber(roomNumber).orElseThrow(() -> new RuntimeException("Room not found"));
            List<Equipment> equipment = equipmentRepository.findByRoom(room);
            return ResponseEntity.ok(equipment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create new equipment (Admin only)
     * @param equipment Equipment to create
     * @return Created equipment
     */
    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@Valid @RequestBody Equipment equipment) {
        try {
            Equipment createdEquipment = equipmentRepository.save(equipment);
            return ResponseEntity.ok(createdEquipment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}