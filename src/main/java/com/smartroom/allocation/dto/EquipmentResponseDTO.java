package com.smartroom.allocation.dto;

import com.smartroom.allocation.entity.Equipment;
import com.smartroom.allocation.entity.EquipmentType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EquipmentResponseDTO {
    // Getters and setters
    private Long id;
    private String name;
    private EquipmentType type;
    private String description;
    private boolean working;
    private Long roomId;
    private String roomNumber;

    public EquipmentResponseDTO(Equipment equipment) {
        this.id = equipment.getId();
        this.name = equipment.getName();
        this.type = equipment.getType();
        this.description = equipment.getDescription();
        this.working = equipment.isWorking();
        this.roomId = equipment.getRoom() != null ? equipment.getRoom().getId() : null;
        this.roomNumber = equipment.getRoom() != null ? equipment.getRoom().getRoomNumber() : null;
    }

}