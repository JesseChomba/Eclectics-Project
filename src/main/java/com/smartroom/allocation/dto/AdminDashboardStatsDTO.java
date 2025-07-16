package com.smartroom.allocation.dto;

public class AdminDashboardStatsDTO {
    private long totalBookingsEver;
    private long totalRoomsAvailable;
    private long totalActiveRooms;
    private long totalUpcomingBookings;
    private long totalActiveUsers;

    public AdminDashboardStatsDTO(long totalBookingsEver, long totalRoomsAvailable, long totalActiveRooms, long totalUpcomingBookings, long totalActiveUsers) {
        this.totalBookingsEver = totalBookingsEver;
        this.totalRoomsAvailable = totalRoomsAvailable;
        this.totalActiveRooms = totalActiveRooms;
        this.totalUpcomingBookings = totalUpcomingBookings;
        this.totalActiveUsers = totalActiveUsers;
    }

    // Getters
    public long getTotalBookingsEver() {
        return totalBookingsEver;
    }

    public long getTotalRoomsAvailable() {
        return totalRoomsAvailable;
    }

    public long getTotalActiveRooms() {
        return totalActiveRooms;
    }

    public long getTotalUpcomingBookings() {
        return totalUpcomingBookings;
    }

    public long getTotalActiveUsers() {
        return totalActiveUsers;
    }

    // Setters (optional, but good for JavaBeans compliance if you were building it step-by-step)
    public void setTotalBookingsEver(long totalBookingsEver) {
        this.totalBookingsEver = totalBookingsEver;
    }

    public void setTotalRoomsAvailable(long totalRoomsAvailable) {
        this.totalRoomsAvailable = totalRoomsAvailable;
    }

    public void setTotalActiveRooms(long totalActiveRooms) {
        this.totalActiveRooms = totalActiveRooms;
    }

    public void setTotalUpcomingBookings(long totalUpcomingBookings) {
        this.totalUpcomingBookings = totalUpcomingBookings;
    }

    public void setTotalActiveUsers(long totalActiveUsers) {
        this.totalActiveUsers = totalActiveUsers;
    }
}
