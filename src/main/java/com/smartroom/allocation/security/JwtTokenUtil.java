package com.smartroom.allocation.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    // This line gets a logger specific to JwtAuthenticationFilter
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {

        try { // ADD THIS TRY-CATCH
            return getClaimFromToken(token, Claims::getSubject); // [cite: 235]
        } catch (Exception e) {
            logger.error("Error getting username from token: " + token, e); // ADD THIS
            throw e; // Re-throw to maintain behavior
        }
    }

    /**
     * Extract expiration date from JWT token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get all claims from JWT token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * Check if JWT token is expired
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Generate JWT token for user
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Create JWT token with claims and subject
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * Validate JWT token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {

        try { // ADD THIS TRY-CATCH
            final String username = getUsernameFromToken(token); // [cite: 243]
            boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token)); // [cite: 244]
            logger.info("Validating token for user: " + username + ". Is valid? " + isValid); // ADD THIS
            if (isTokenExpired(token)) {
                logger.warn("Token for user " + username + " is expired."); // ADD THIS
            }
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating token: " + token, e); // ADD THIS
            return false;
        }
    }
}