package com.smartroom.allocation.dto;

import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.entity.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String department;
    private UserRole role;
    private Boolean active;
    private Integer points;
    private Integer usageStreak;
    private int totalBookings; // This is a @Transient field from User entity
    private LocalDateTime createdAT;
    public UserResponseDTO() {
    }

    // Constructor to convert a User entity to a UserResponseDTO
    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.department = user.getDepartment();
        this.role = user.getRole();
        this.active = user.isActive();
        this.points = user.getPoints();
        this.usageStreak = user.getUsageStreak();
        this.totalBookings = user.getTotalBookings(); // Assuming this is populated in service
        this.createdAT=user.getCreatedAt();
    }


}
