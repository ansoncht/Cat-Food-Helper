package com.ansoncht.catfoodtracker.security;

import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private static final String TEST_SECRET = "testSecretKeyWhichShouldBeAtLeast256BitsLong";
    private static final int TEST_EXPIRATION = 3600000;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        this.jwtService = new JwtService(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    void testGenerateToken_ValidUsername_ShouldReturnToken() {
        String token = jwtService.generateToken("testuser");

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateToken_InvalidSecret_ShouldThrowException() {
        JwtService weakJwtService = new JwtService("short", TEST_EXPIRATION);

        WeakKeyException exception = assertThrows(WeakKeyException.class,
                () -> weakJwtService.generateToken("testuser"));
        assertNotNull(exception);
    }

    @Test
    void testValidateToken_ValidToken_ShouldReturnTrue() {
        String token = jwtService.generateToken("testuser");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalidToken";

        assertFalse(jwtService.validateToken(invalidToken));
    }

    @Test
    void testGetUsernameFromToken_ValidToken_ShouldReturnUsername() {
        String username = "testuser";
        String token = jwtService.generateToken(username);

        assertEquals(username, jwtService.getUsernameFromToken(token));
    }

    @Test
    void testGetUsernameFromToken_InvalidToken_ShouldThrowException() {
        String invalidToken = "invalidToken";

        Exception exception = assertThrows(RuntimeException.class,
                () -> jwtService.getUsernameFromToken(invalidToken));
        assertNotNull(exception.getMessage());
    }
}
