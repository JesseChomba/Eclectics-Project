package com.smartroom.allocation.security;

import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;



@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by username for Spring Security authentication
     * @param username Username to load
     * @return UserDetails object
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("CustomUserDetailsService: Loading user by username: " + username); // ADD THIS
        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                .orElseThrow(() -> {
                    logger.warn("CustomUserDetailsService: User not found with username: " + username); // ADD THIS
                    return new UsernameNotFoundException("User not found: " + username);
                });
        logger.info("CustomUserDetailsService: User found: " + user.getUsername() + ", Role: " + user.getRole() + ", Active: " + user.isActive()); // ADD THIS


        // Convert user role to Spring Security authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()));
        logger.info("CustomUserDetailsService: Authorities created: " + authorities); // ADD THIS


        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isActive())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}