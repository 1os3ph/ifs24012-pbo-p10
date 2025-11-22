package org.delcom.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;

public class JwtUtilTests {

    @Test
    @DisplayName("Berbagai pengujian JwtUtil")
    public void testVariousJwtUtil() {
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);

        JwtUtil jwtUtil = new JwtUtil();

        // Extract userId dari token yang valid
        {
            UUID extractedUserId = JwtUtil.extractUserId(token);
            assertEquals(userId, extractedUserId);
        }

        // Extract userId dari token yang tidak valid
        {
            UUID extractedUserId = JwtUtil.extractUserId(token + "invalid");
            assertEquals(null, extractedUserId);
        }

        // Validasi token yang valid
        {
            boolean isValid = JwtUtil.validateToken(token, false);
            assertEquals(true, isValid);
        }

        // Validasi token yang tidak valid
        {
            boolean isValid = JwtUtil.validateToken(token + "invalid", false);
            assertEquals(false, isValid);
        }

        // Validasi token yang expired dengan ignoreExpired = false
        {
            String expiredToken = Jwts.builder()
                    .subject(userId.toString())
                    .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 3)) // 3 jam yang lalu
                    .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 1)) // expired 1 jam yang lalu
                    .signWith(JwtUtil.getKey()) // Perlu menambahkan method getKey() di JwtUtil
                    .compact();

            boolean isValid = JwtUtil.validateToken(expiredToken, false);
            assertEquals(false, isValid);
        }

        // Validasi token yang expired dengan ignoreExpired = true
        {
            String expiredToken = Jwts.builder()
                    .subject(userId.toString())
                    .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 3)) // 3 jam yang lalu
                    .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 1)) // expired 1 jam yang lalu
                    .signWith(JwtUtil.getKey()) // Perlu menambahkan method getKey() di JwtUtil
                    .compact();

            boolean isValid = JwtUtil.validateToken(expiredToken, true);
            assertEquals(true, isValid);
        }

    }

}