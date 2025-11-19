package com.pridebank.token.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // Uganda/East Africa Time Zone (EAT - UTC+3)
    private static final ZoneId UGANDA_TIMEZONE = ZoneId.of("Africa/Kampala");

    // ISO 8601 formatter with 7 decimal places for fractional seconds and +03:00 offset
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'+03:00'");

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token with embedded credentials
     */
    public String generateToken(String username, String password) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Encode credentials in Base64 for embedding in token
        String credentials = Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8)
        );

        Map<String, Object> claims = new HashMap<>();
        claims.put("credentials", credentials);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Convert Date to Uganda Time (EAT - UTC+3) in ISO 8601.
     * Format: 2025-11-19T13:42:41.0000000+03:00
     */
    public String formatDateToISO8601(Date date) {
        Instant instant = date.toInstant();
        ZonedDateTime ugandaTime = instant.atZone(UGANDA_TIMEZONE);
        return ugandaTime.format(ISO_FORMATTER);
    }

    /**
     * Get issued date from token as ISO 8601 string in Uganda Time
     */
    public String getIssuedDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date issuedAt = claims.getIssuedAt();
            return formatDateToISO8601(issuedAt);
        } catch (Exception e) {
            log.error("Failed to extract issued date from token", e);
            return null;
        }
    }

    /**
     * Get expiry date from token as ISO 8601 string in Uganda Time
     */
    public String getExpiryDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            return formatDateToISO8601(expiration);
        } catch (Exception e) {
            log.error("Failed to extract expiry date from token", e);
            return null;
        }
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Get embedded credentials from JWT token and decode them
     * Returns array [username, password]
     */
    public String[] getDecodedCredentialsFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String encodedCredentials = claims.get("credentials", String.class);

            if (encodedCredentials == null) {
                throw new IllegalArgumentException("No credentials found in token");
            }

            // Decode Base64 credentials
            byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials);
            String decodedCredentials = new String(decodedBytes, StandardCharsets.UTF_8);

            // Split username:password
            String[] parts = decodedCredentials.split(":", 2);

            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid credentials format in token");
            }

            return parts; // [username, password]

        } catch (Exception e) {
            log.error("Failed to decode credentials from token", e);
            throw new IllegalArgumentException("Failed to extract credentials from token", e);
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

}