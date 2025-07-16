package com.smartroom.allocation.service;

import com.smartroom.allocation.dto.AdminDashboardStatsDTO;
import com.smartroom.allocation.entity.BookingStatus;
import com.smartroom.allocation.entity.RoomStatus;
import com.smartroom.allocation.repository.BookingRepository;
import com.smartroom.allocation.repository.RoomRepository;
import com.smartroom.allocation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminDashboardService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Fetches all necessary statistics for the admin dashboard.
     * @return AdminDashboardStatsDTO containing aggregated data.
     */
    public AdminDashboardStatsDTO getDashboardStats() {
        long totalBookingsEver = bookingRepository.count();
        long totalRoomsAvailable = roomRepository.countByStatus(RoomStatus.AVAILABLE);
        long totalActiveRooms = roomRepository.countByActiveTrue();
        long totalUpcomingBookings = bookingRepository.countUpcomingBookings(LocalDateTime.now());
        long totalActiveUsers = userRepository.countByActiveTrue();

        return new AdminDashboardStatsDTO(
                totalBookingsEver,
                totalRoomsAvailable,
                totalActiveRooms,
                totalUpcomingBookings,
                totalActiveUsers
        );
    }
}
