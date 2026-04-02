package com.example.employee_api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "ZmFrZS1lbXBsb3llZS1hcGktand0LXNlY3JldC1mb3ItbGVhcm5pbmctb25seS0yMDI2LTAzLTI1LXNlY3VyZQ==",
                60,
                7,
                "employee-api-test");
    }

    @Test
    void shouldGenerateAccessTokenWithExpectedClaims() {
        String token = jwtService.generateAccessToken(
                "salmauser",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertEquals("salmauser", jwtService.extractUsername(token));
        assertTrue(jwtService.isAccessToken(token));
        assertFalse(jwtService.isRefreshToken(token));
        assertTrue(jwtService.isTokenValid(token, "salmauser"));
    }

    @Test
    void shouldGenerateRefreshTokenWithLongerExpiry() {
        String token = jwtService.generateRefreshToken("salmauser");

        Instant expiry = jwtService.extractExpiry(token);

        assertTrue(jwtService.isRefreshToken(token));
        assertFalse(jwtService.isAccessToken(token));
        assertTrue(Duration.between(Instant.now(), expiry).toDays() >= 6);
    }
}
