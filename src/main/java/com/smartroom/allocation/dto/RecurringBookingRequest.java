package com.smartroom.allocation.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Data Transfer Object for creating recurring bookings.
 * Defines the request payload structure.
 */
public class RecurringBookingRequest {
    private Long roomId; //changed from roomNumber
    private LocalDate semesterStartDate;
    private LocalDate semesterEndDate;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private String notes;

    // Specifies the interval: 1 for weekly, 2 for bi-weekly, etc. Defaults to 1.
    private int intervalWeeks = 1;

    // Getters and Setters
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public LocalDate getSemesterStartDate() { return semesterStartDate; }
    public void setSemesterStartDate(LocalDate semesterStartDate) { this.semesterStartDate = semesterStartDate; }

    public LocalDate getSemesterEndDate() { return semesterEndDate; }
    public void setSemesterEndDate(LocalDate semesterEndDate) { this.semesterEndDate = semesterEndDate; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getIntervalWeeks() { return intervalWeeks; }
    public void setIntervalWeeks(int intervalWeeks) { this.intervalWeeks = intervalWeeks; }
}