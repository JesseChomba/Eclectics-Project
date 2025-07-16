package com.smartroom.allocation.service;

import com.smartroom.allocation.dto.UserResponseDTO;
import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.entity.UserRole;
import com.smartroom.allocation.repository.BookingRepository;
import com.smartroom.allocation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Register a new user
     * @param user User to register
     * @return Registered user
     */
    public User registerUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser= userRepository.save(user);
        notificationService.sendWelcomeEmail(savedUser); //email welcoming user
        return savedUser;
    }

    /*Get all users available
     * for admins only
     * Now updated to return DTOs to hide sensitive details like password*/
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        //populate totalBookings and then map to DTOs
//        for (User user : users) {
//            int count = bookingRepository.countByUser(user);
//            user.setTotalBookings(count); // Ensure this setter exists
//        }
//        return users;
        return users.stream().map(user -> {
            int count= bookingRepository.countByUser(user);
            user.setTotalBookings(count);
            return new UserResponseDTO(user); //converts user entity to DTO
        }).collect(Collectors.toList());
    }

    /**
     * Find user by username (for authentication)
     * @param username Username to search for
     * @return User if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get all active users
     * @return List of active users
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    /**
     * Get users by role
     * @param role User role to filter by
     * @return List of users with specified role
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Update user points (for gamification)
     * @param userId User ID
     * @param points Points to add
     */
    public void updateUserPoints(Long userId, int points) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPoints(user.getPoints() + points);
            userRepository.save(user);
        }
    }

    /**
     * Get leaderboard (top users by points)
     * @return List of top users
     */
    public List<User> getLeaderboard() {
        return userRepository.findTopUsersByPoints();
    }

    /**
     * Find user by ID
     * @param id User ID
     * @return User if found
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Updates the password for a given user after verifying the old password
     * @param userId the ID of the user.
     * @param oldPassword The user's current password.
     * @param newPassword The new password to set.
     * @throws BadCredentialsException if the old password does not match
     * */
    public void updateUserPassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if the old password matches the stored password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Incorrect old password");
        }

        // Encode the new password and save it
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

    }

    /*
     * CORRECTED: Update user details by ID
     * Only updates non-null fields from the userUpdate object to
     * prevent accidentally nullifying existing data
     * @param id The ID of the user to update
     * @param userUpdate An object containing the new user data.
     * @return An Optional containing the updated user, or empty if not found.
     */
    public Optional<UserResponseDTO> updateUserById(Long id, User userUpdate) { //changed return type
        return userRepository.findById(id).map(user -> {
            // Check if Username or email that is being updated already exists
            if(userUpdate.getUsername() !=null && !user.getUsername().equals(userUpdate.getUsername()) && userRepository.existsByUsername(userUpdate.getUsername())){
                throw new RuntimeException("Username already exists!");
            }
            if(userUpdate.getEmail() !=null && !user.getEmail().equals(userUpdate.getEmail()) && userRepository.existsByEmail(userUpdate.getEmail())){
                throw new RuntimeException("Username already exists!");
            }
            // Only update fields if they are provided in the request body (i.e., not null)
            if (userUpdate.getFullName() != null) {
                user.setFullName(userUpdate.getFullName());
            }
            // Removed the duplicate user.setFullName(userUpdate.getFullName());

            if (userUpdate.getUsername() != null) {
                user.setUsername(userUpdate.getUsername());
            }
            if (userUpdate.getEmail() != null) {
                user.setEmail(userUpdate.getEmail());
            }
            if (userUpdate.getDepartment() != null) {
                user.setDepartment(userUpdate.getDepartment());
            }
            if (userUpdate.getRole() != null) {
                user.setRole(userUpdate.getRole());
            }

            // Now that 'active', 'points', 'usageStreak' are wrapper types (Boolean, Integer),
            // they can be null if omitted from the JSON.
            // Apply conditional updates for them as well.
            if (userUpdate.isActive() != null) {
                user.setActive(userUpdate.isActive());
            }
            if (userUpdate.getPoints() != null) {
                user.setPoints(userUpdate.getPoints());
            }
            if (userUpdate.getUsageStreak() != null) {
                user.setUsageStreak(userUpdate.getUsageStreak());
            }

            User updatedUser = userRepository.save(user);

            //populate totalBookings before converting to DTO
            int count = bookingRepository.countByUser(updatedUser);
            updatedUser.setTotalBookings(count);
            notificationService.sendUserUpdateNotification(updatedUser);
            return new UserResponseDTO(updatedUser); // Return DTO
        });
    }

    public boolean deleteUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            notificationService.sendDeletionNotification(userOpt.get()); // Add this
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Find user by username and populate total bookings
     * @param username Username to search for
     * @return userOpt if found
     * */
    public Optional<User> findAndPopulateTotalBookings(String username) {
        Optional<User> userOpt = findByUsername(username);
        userOpt.ifPresent(user -> {
            int count = bookingRepository.countByUser(user);
            user.setTotalBookings(count);
        });
        return userOpt;
    }

}
