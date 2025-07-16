package com.smartroom.allocation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class BookingUpdateDTO {

    // @NotNull(message = "Start time is required") removed for the update functionality
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    //@NotNull(message = "End time is required") removed for the update functionality
    private LocalDateTime endTime;

    private String purpose;

    // Getters and Setters
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}