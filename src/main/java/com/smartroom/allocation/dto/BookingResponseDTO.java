package com.smartroom.allocation.dto;

import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.BookingStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Added for consistent date/time formatting

public class BookingResponseDTO {
    private Long id;
    private Long roomId;
    private String roomNumber; // New field
    private String roomName;   // New field
    private String lecturerName; // Existing field (user's full name)
    private String username;     // New field
    private String purpose;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // DateTimeFormatter for consistent output (optional, but good practice if you want string dates)
    // private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    // Constructor
    public BookingResponseDTO(Booking booking) {
        this.id = booking.getId();
        this.roomId=booking.getRoom().getId(); //added
        this.roomNumber = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : null; //added
        this.startTime = booking.getStartTime();
        this.endTime = booking.getEndTime();
        this.purpose= booking.getPurpose();
        this.status = booking.getStatus();
        this.createdAt = booking.getCreatedAt();
        this.updatedAt = booking.getUpdatedAt();

        if (booking.getRoom() != null) {
            this.roomId = booking.getRoom().getId();
            this.roomNumber = booking.getRoom().getRoomNumber(); // Populate new field
            this.roomName = booking.getRoom().getName();         // Populate new field
        } else {
            this.roomId = null;
            this.roomNumber = null;
            this.roomName = null;
        }

        if (booking.getUser() != null) {
            this.lecturerName = booking.getUser().getFullName();
            this.username = booking.getUser().getUsername(); // Populate new field
        } else {
            this.lecturerName = null;
            this.username = null;
        }
    }

    // Default constructor (important for Spring/Jackson deserialization in some cases)
    public BookingResponseDTO() {
    }

    // Getters and setters (existing ones remain, new ones added below)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    // New getters and setters for roomNumber and roomName

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }

    // New getter and setter for username
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPurpose(){return purpose;}

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
