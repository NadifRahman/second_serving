package com.secondserving.secondserving.config.security;

import com.secondserving.secondserving.domain.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilsServiceTest {

    private static final String TEST_SECRET = "Zm9yLXRlc3RzLW9ubHktdXNlLWEtbG9uZy1yYW5kb20tYmFzZTY0LXNlY3JldC12YWx1ZS1oZXJl";

    @Test
    void generateToken_extractUsername_returnsOriginalUsername() {
        JwtUtilsService underTest = new JwtUtilsService(TEST_SECRET, 3_600_000);
        User user = new User("alice", "hashed-password", "Alice Example", "alice@example.com");

        String token = underTest.generateToken(new UserDetailsImpl(user));

        assertEquals("alice", underTest.extractUsername(token));
    }

    @Test
    void isTokenValid_returnsTrueForMatchingUser() {
        JwtUtilsService underTest = new JwtUtilsService(TEST_SECRET, 3_600_000);
        User user = new User("alice", "hashed-password", "Alice Example", "alice@example.com");
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String token = underTest.generateToken(userDetails);

        assertTrue(underTest.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenExpired_throwsForExpiredToken() {
        JwtUtilsService underTest = new JwtUtilsService(TEST_SECRET, -1);
        User user = new User("alice", "hashed-password", "Alice Example", "alice@example.com");
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String token = underTest.generateToken(userDetails);

        assertThrows(ExpiredJwtException.class, () -> underTest.isTokenExpired(token));
    }
}
