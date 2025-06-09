package com.smartroom.allocation.dto;

import com.smartroom.allocation.entity.Equipment;
import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.BookingStatus;
import com.smartroom.allocation.entity.RoomStatus;
import com.smartroom.allocation.entity.RoomType;
import java.util.List;
import java.util.stream.Collectors;

public class RoomResponseDTO {
    private Long id;
    private String roomNumber;
    private String name;
    private int capacity;
    private String building;
    private String floor;
    private String location;
    private RoomType roomType;
    private RoomStatus status;
    private boolean isActive;
    private List<EquipmentSummaryDTO> equipment;
    private List<BookingSummaryDTO> bookings;

    // Inner class to summarize equipment
    public static class EquipmentSummaryDTO {
        private Long id;
        private String name;

        public EquipmentSummaryDTO(Equipment equipment) {
            this.id = equipment.getId();
            this.name = equipment.getName();
        }

        public Long getId() { return id; }
        public String getName() { return name; }
    }

    // Inner class to summarize bookings
    public static class BookingSummaryDTO {
        private Long id;
        private String purpose;
        private BookingStatus status;

        public BookingSummaryDTO(Booking booking) {
            this.id = booking.getId();
            this.purpose = booking.getPurpose();
            this.status = booking.getStatus();
        }

        public Long getId() { return id; }
        public String getPurpose() { return purpose; }
        public BookingStatus getStatus() { return status; }
    }

    // Constructor
    public RoomResponseDTO(com.smartroom.allocation.entity.Room room) {
        this.id = room.getId();
        this.roomNumber = room.getRoomNumber();
        this.name = room.getName();
        this.capacity = room.getCapacity();
        this.building = room.getBuilding();
        this.floor = room.getFloor();
        this.location = room.getLocation();
        this.roomType = room.getRoomType();
        this.status = room.getStatus();
        this.isActive = room.isActive();
        this.equipment = room.getEquipment() != null ? room.getEquipment().stream()
                .map(EquipmentSummaryDTO::new)
                .collect(Collectors.toList()) : null;
        this.bookings = room.getBookings() != null ? room.getBookings().stream()
                .map(BookingSummaryDTO::new)
                .collect(Collectors.toList()) : null;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public List<EquipmentSummaryDTO> getEquipment() { return equipment; }
    public void setEquipment(List<EquipmentSummaryDTO> equipment) { this.equipment = equipment; }

    public List<BookingSummaryDTO> getBookings() { return bookings; }
    public void setBookings(List<BookingSummaryDTO> bookings) { this.bookings = bookings; }
}