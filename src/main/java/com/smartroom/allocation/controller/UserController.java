package com.smartroom.allocation.controller;

import com.smartroom.allocation.dto.PasswordUpdateRequest;
import com.smartroom.allocation.dto.UserResponseDTO;
import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /*
     * Getting all the users available in Db
     * Only available for Admins*/
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                //response.put("Token", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            //changed to call updated service method that returns DTOs
            List<UserResponseDTO> users = userService.getAllUsers();
            response.put("Status", 1);
            response.put("Message", "Users retrieved successfully");
            response.put("Data", users);
            //response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve users: " + e.getMessage());
            response.put("Data", "");
            //response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // User: Get own profile  ---Currently logged in user is able to get their information
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getOwnProfile(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.getName() == null) {
                response.put("Status", 0);
                response.put("Message", "Authentication required");
                response.put("Data", "");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Optional<User> currentUser = userService.findAndPopulateTotalBookings(auth.getName());
            //Optional<User> currentUser = userService.findByUsername(auth.getName());  old
            if (!currentUser.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "User not found");
                response.put("Data", "");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            currentUser.get().setPassword(null); // Ensure password is not exposed
            response.put("Status", 1);
            response.put("Message", "Profile retrieved successfully");
            response.put("Data", currentUser.get());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve profile: " + e.getMessage());
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user by ID (Admin only)
     * @param id User ID
     * @return User details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String,Object>> getUserById(@PathVariable Long id){
        Map<String, Object> response= new HashMap<>();
        try {
            Optional<User> userOpt = userService.findById(id);
            if (!userOpt.isPresent()){
                response.put("Status",0);
                response.put("Message","User not found with id: " +id);
                response.put("Data","");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            User user = userOpt.get();
            userService.findAndPopulateTotalBookings(user.getUsername()); //update total bookings
            user.setPassword(null); //avoid exposing password
            response.put("Status",1);
            response.put("Message", "User retrieved successfully");
            response.put("Data",user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status",0);
            response.put("Message","Failed to retrieve user: "+ e.getMessage());
            response.put("Data","");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Register a new user
     * @param user User details
     * @return Registered user
     */
//    @PostMapping("/register")
//    public ResponseEntity<User> registerUser(@RequestBody User user) {
//        try {
//            User registeredUser = userService.registerUser(user);
//            // Don't return password in response
//            registeredUser.setPassword(null);
//            return ResponseEntity.ok(registeredUser);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            User registeredUser = userService.registerUser(user);
            response.put("Status", 1);
            response.put("Message", "User registered successfully");
            response.put("Data", registeredUser);
            //response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("Status", 0);
            response.put("Message", e.getMessage());
            response.put("Data", "");
            //response.put("Token", "");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get leaderboard (for gamification)
     * @return List of top users by points
     */
//    @GetMapping("/leaderboard")
//    public ResponseEntity<List<User>> getLeaderboard() {
//        List<User> leaderboard = userService.getLeaderboard();
//        // Remove passwords from response
//        leaderboard.forEach(user -> user.setPassword(null));
//        return ResponseEntity.ok(leaderboard);
//    }

    @GetMapping("/leaderboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> leaderboard = userService.getLeaderboard();
            // Remove passwords from response
            leaderboard.forEach(user -> user.setPassword(null));

            response.put("Status", 1);
            response.put("Message", "Leaderboard retrieved successfully");
            response.put("Data", leaderboard);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve leaderboard: " + e.getMessage());
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // Get user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            user.get().setPassword(null);
            response.put("Status", 1);
            response.put("Message", "User retrieved successfully");
            response.put("Data", user.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("Status", 0);
            response.put("Message", "User not found");
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    // Admin: Update user by ID
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserById(@PathVariable Long id, @RequestBody User userUpdate) {
        Map<String, Object> response = new HashMap<>();
        Optional<UserResponseDTO> updatedUserDTO = userService.updateUserById(id, userUpdate);
        if (updatedUserDTO.isPresent()) {
            response.put("Status", 1);
            response.put("Message", "User updated successfully");
            response.put("Data", updatedUserDTO.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("Status", 0);
            response.put("Message", "User not found");
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // User: Update own profile
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateOwnProfile(Authentication auth, @RequestBody User userUpdate) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> current = userService.findByUsername(auth.getName());
        if (!current.isPresent()) {
            response.put("Status", 0);
            response.put("Message", "User not found");
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        Optional<UserResponseDTO> updated = userService.updateUserById(current.get().getId(), userUpdate);
        if (updated.isPresent()) {
            response.put("Status", 1);
            response.put("Message", "Profile updated successfully");
            response.put("Data", updated.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("Status", 0);
            response.put("Message", "Update failed");
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    //User: Update own password
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateOwnPassword(@Valid @RequestBody PasswordUpdateRequest passwordRequest, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            User currentUser = userService.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            userService.updateUserPassword(
                    currentUser.getId(),
                    passwordRequest.getOldPassword(),
                    passwordRequest.getNewPassword()
            );

            response.put("Status", 1);
            response.put("Message", "Password updated successfully");
            return ResponseEntity.ok(response);

        } catch ( BadCredentialsException e) {
            response.put("Status", 0);
            response.put("Message", "Incorrect old password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to update password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // Admin: Delete user by ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUserById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = userService.deleteUserById(id);
        if (deleted) {
            response.put("Status", 1);
            response.put("Message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("Status", 0);
            response.put("Message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // User: Delete own account
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> deleteOwnAccount(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> current = userService.findByUsername(auth.getName());
        if (!current.isPresent()) {
            response.put("Status", 0);
            response.put("Message", "User not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        boolean deleted = userService.deleteUserById(current.get().getId());
        if (deleted) {
            response.put("Status", 1);
            response.put("Message", "Account deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("Status", 0);
            response.put("Message", "Deletion failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}