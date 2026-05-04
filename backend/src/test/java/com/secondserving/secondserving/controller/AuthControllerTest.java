package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.config.security.JwtUtilsService;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.dto.AuthSessionDto;
import com.secondserving.secondserving.dto.LoginRequestDto;
import com.secondserving.secondserving.dto.SignupRequestDto;
import com.secondserving.secondserving.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthControllerTest {

    private static final Instant EXPIRES_AT = Instant.parse("2026-05-04T18:00:00Z");

    @Test
    void signup_returnsCreatedSessionResponseAndSetsCookie() {
        StubUserService userService = new StubUserService();
        StubJwtUtilsService jwtUtilsService = new StubJwtUtilsService();
        AuthController underTest = newAuthController(userService, jwtUtilsService);
        User user = new User("testuser", "hashed-password", "Test User", "test@example.com");
        userService.userToRegister = user;
        jwtUtilsService.tokenToReturn = "jwt-token";

        ResponseEntity<AuthSessionDto> response = underTest.signup(
                new SignupRequestDto("testuser", "password", "Test User", "test@example.com")
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AuthSessionDto body = assertInstanceOf(AuthSessionDto.class, response.getBody());
        assertEquals(user.getUserId(), body.user().userId());
        assertEquals("testuser", body.user().username());
        assertEquals("Test User", body.user().fullName());
        assertEquals("test@example.com", body.user().email());
        assertEquals(EXPIRES_AT, body.expiresAt());

        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("access_token=jwt-token"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("Path=/"));
        assertTrue(setCookie.contains("SameSite=Lax"));
        assertTrue(setCookie.contains("Max-Age=3600"));
    }

    @Test
    void signup_returnsBadRequestWhenRegistrationFails() {
        StubUserService userService = new StubUserService();
        StubJwtUtilsService jwtUtilsService = new StubJwtUtilsService();
        AuthController underTest = newAuthController(userService, jwtUtilsService);
        userService.registrationException = new IllegalArgumentException("Username already exists");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                underTest.signup(new SignupRequestDto("testuser", "password", "Test User", "test@example.com")));
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void login_returnsSessionResponseAndSetsCookieWhenCredentialsAreValid() {
        StubUserService userService = new StubUserService();
        StubJwtUtilsService jwtUtilsService = new StubJwtUtilsService();
        AuthController underTest = newAuthController(userService, jwtUtilsService);
        User user = new User("testuser", "hashed-password", "Test User", "test@example.com");
        userService.authenticateUserResult = true;
        userService.userByUsername = user;
        jwtUtilsService.tokenToReturn = "jwt-token";

        ResponseEntity<AuthSessionDto> response = underTest.login(new LoginRequestDto("testuser", "password"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthSessionDto body = assertInstanceOf(AuthSessionDto.class, response.getBody());
        assertEquals(user.getUserId(), body.user().userId());
        assertEquals("testuser", body.user().username());
        assertEquals(EXPIRES_AT, body.expiresAt());

        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("access_token=jwt-token"));
        assertTrue(setCookie.contains("HttpOnly"));
    }

    @Test
    void login_returnsUnauthorizedWhenCredentialsAreInvalid() {
        StubUserService userService = new StubUserService();
        StubJwtUtilsService jwtUtilsService = new StubJwtUtilsService();
        AuthController underTest = newAuthController(userService, jwtUtilsService);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                underTest.login(new LoginRequestDto("testuser", "wrong-password")));
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void logout_clearsAuthCookie() {
        AuthController underTest = newAuthController(new StubUserService(), new StubJwtUtilsService());

        ResponseEntity<Void> response = underTest.logout();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("access_token="));
        assertTrue(setCookie.contains("Max-Age=0"));
        assertTrue(setCookie.contains("HttpOnly"));
    }

    private static AuthController newAuthController(UserService userService, JwtUtilsService jwtUtilsService) {
        return new AuthController(userService, jwtUtilsService, "access_token", false, "Lax", 3_600_000);
    }

    private static class StubUserService extends UserService {
        private User userToRegister;
        private User userByUsername;
        private boolean authenticateUserResult;
        private IllegalArgumentException registrationException;

        StubUserService() {
            super(null, null);
        }

        @Override
        public User registerUser(String username, String rawPassword, String fullName, String email) {
            if (registrationException != null) {
                throw registrationException;
            }
            return userToRegister;
        }

        @Override
        public boolean authenticateUser(String username, String rawPassword) {
            return authenticateUserResult;
        }

        @Override
        public User getUserByUsername(String username) {
            return userByUsername;
        }
    }

    private static class StubJwtUtilsService extends JwtUtilsService {
        private String tokenToReturn;

        StubJwtUtilsService() {
            super("Zm9yLXRlc3RzLW9ubHktdXNlLWEtbG9uZy1yYW5kb20tYmFzZTY0LXNlY3JldC12YWx1ZS1oZXJl", 3_600_000);
        }

        @Override
        public JwtUtilsService.GeneratedJwt generateTokenWithExpiration(org.springframework.security.core.userdetails.UserDetails userDetails) {
            return new JwtUtilsService.GeneratedJwt(tokenToReturn, EXPIRES_AT);
        }
    }
}
