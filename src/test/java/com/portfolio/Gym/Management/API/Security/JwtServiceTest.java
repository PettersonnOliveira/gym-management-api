package com.portfolio.Gym.Management.API.Security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    private static final String VALID_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void rejectsMissingSecret() {
        JwtService jwtService = new JwtService("", 3600000);
        assertThrows(IllegalStateException.class, jwtService::init);
    }

    @Test
    void rejectsSecretShorterThan32Characters() {
        JwtService jwtService = new JwtService("abc", 3600000);
        assertThrows(IllegalStateException.class, jwtService::init);
    }

    @Test
    void rejectsBase64KeyThatDecodesToLessThan32Bytes() {
        // 32 characters, but decodes to only 24 bytes
        JwtService jwtService = new JwtService("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3", 3600000);
        assertThrows(IllegalStateException.class, jwtService::init);
    }

    @Test
    void acceptsValidSecret() {
        JwtService jwtService = new JwtService(VALID_SECRET, 3600000);
        assertDoesNotThrow(jwtService::init);
    }
}
