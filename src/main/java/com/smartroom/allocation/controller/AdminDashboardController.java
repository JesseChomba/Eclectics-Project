package com.smartroom.allocation.controller;

import com.smartroom.allocation.dto.AdminDashboardStatsDTO;
import com.smartroom.allocation.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*") // Adjust CORS as needed for your frontend
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /**
     * Fetches aggregated statistics for the admin dashboard.
     * Accessible only by users with 'ADMIN' role.
     * @return ResponseEntity with standardized JSON response containing dashboard stats.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> response = new HashMap<>();
        try {
            AdminDashboardStatsDTO stats = adminDashboardService.getDashboardStats();
            response.put("Status", 1);
            response.put("Message", "Dashboard statistics retrieved successfully");
            response.put("Data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve dashboard statistics: " + e.getMessage());
            response.put("Data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
