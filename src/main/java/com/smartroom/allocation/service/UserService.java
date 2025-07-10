package com.smartroom.allocation.service;

import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.entity.UserRole;
import com.smartroom.allocation.repository.BookingRepository;
import com.smartroom.allocation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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
     * for admins only*/
    public List<User> getAllUsers() {
//        return userRepository.findAll();
        List<User> users = userRepository.findAll();
        for (User user : users) {
            int count = bookingRepository.countByUser(user);
            user.setTotalBookings(count); // Ensure this setter exists
        }
        return users;
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

    /*
    * CORRECTED: Update user details by ID
    * Only updates non-null fields from the userUpdate object to
    * prevent accidentally nullifying existing data
    * @param id The ID of the user to annotate
    * @param userupdate An object containing the new user data.
    * @return An Optional containing the updated user, or empty if not found.*/
    public Optional<User> updateUserById(Long id, User userUpdate) {
        return userRepository.findById(id).map(user -> {
            //only update fields if they are provided in the request body
            if (userUpdate.getFullName() !=null){
                user.setFullName(userUpdate.getFullName());
            }
            if (userUpdate.getUsername() !=null){
                user.setUsername(userUpdate.getUsername()); //added
            }
            user.setFullName(userUpdate.getFullName());
            if (userUpdate.getEmail() !=null){
                user.setEmail(userUpdate.getEmail()); //added
            }
            if (userUpdate.getDepartment() !=null){
                user.setDepartment(userUpdate.getDepartment());
            }
            if (userUpdate.getRole() !=null){
                user.setRole(userUpdate.getRole());
            }
            user.setActive(userUpdate.isActive());
            user.setRole(userUpdate.getRole());
            user.setPoints(userUpdate.getPoints());
            user.setUsageStreak(userUpdate.getUsageStreak());
            return userRepository.save(user);
        });
    }

    //    public boolean deleteUserById(Long id) {    old implementation!
//        if (userRepository.existsById(id)) {
//            userRepository.deleteById(id);
//            return true;
//        }Old implementation
//        return false;
//    }
    public boolean deleteUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            notificationService.sendDeletionNotification(userOpt.get()); // Add this
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<User> findAndPopulateTotalBookings(String username) {
        Optional<User> userOpt = findByUsername(username);
        userOpt.ifPresent(user -> {
            int count = bookingRepository.countByUser(user);
            user.setTotalBookings(count);
        });
        return userOpt;
    }

}