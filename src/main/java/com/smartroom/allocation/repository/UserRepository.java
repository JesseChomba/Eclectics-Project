package com.smartroom.allocation.repository;

import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username (for authentication)
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find all active users
    List<User> findByActiveTrue();

    // Find users by role
    List<User> findByRole(UserRole role);

    // Find users by department
    List<User> findByDepartment(String department);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find top users by points (for gamification leaderboard)
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.points DESC")
    List<User> findTopUsersByPoints();
}