package com.smartroom.allocation.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Equipment name is required")
    private String name;

    @Enumerated(EnumType.STRING)
    private EquipmentType type;

    private String description;
    private boolean working = true;

    @ManyToOne
    @JoinColumn(name = "room_id")
    @JsonBackReference
    private Room room;

    public Equipment() {}

    public Equipment(String name, EquipmentType type, String description, Room room) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.room = room;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isWorking() { return working; }
    public void setWorking(boolean working) { this.working = working; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
}