package com.smartroom.allocation.controller;

import com.smartroom.allocation.security.JwtTokenUtil;
import com.smartroom.allocation.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Login endpoint - returns standardized response with status, message, data, and token
     * @param loginRequest Contains username and password
     * @return ResponseEntity with standardized JSON response
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword())
            );

            // Load user details and generate token
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            String token = jwtTokenUtil.generateToken(userDetails);

            // Populate Data field with username and role
            Map<String, String> userData = new HashMap<>();
            userData.put("username", userDetails.getUsername());
            userData.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

            // Success response
            response.put("Status", 1);
            response.put("Message", "Login successful");
            response.put("Data", userData);
            response.put("Token", token);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            // Failure response
            response.put("Status", 0);
            response.put("Message", "Invalid credentials");
            response.put("Data", "");
            response.put("Token", "");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            // Handle other unexpected exceptions
            response.put("Status", 0);
            response.put("Message", "Authentication failed: " + e.getMessage());
            response.put("Data", "");
            response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("Status", 1);
        response.put("Message", "Logout successful.");
        response.put("Data", "");
        response.put("Token", "");
        return ResponseEntity.ok(response);
    }
    // Inner classes for request/response
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}