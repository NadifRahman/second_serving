package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.config.security.JwtUtilsService;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.dto.AuthResponseDto;
import com.secondserving.secondserving.dto.LoginRequestDto;
import com.secondserving.secondserving.dto.SignupRequestDto;
import com.secondserving.secondserving.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AuthControllerTest {

    @Test
    void signup_returnsCreatedTokenResponse() {
        StubUserService userService = new StubUserService();
        StubJwtUtilsService jwtUtilsService = new StubJwtUtilsService();
        AuthController underTest = new AuthController(userService, jwtUtilsService);
        User user = new User("testuser", "hashed-password", "Test User", "test@example.com");
        userService.userToRegister = user;
        jwtUtilsService.tokenToReturn = "jwt-token";

        ResponseEntity<?> response = underTest.signup(
                new SignupRequestDto("testuser", "password", "Test User", "test@example.com")
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AuthResponseDto body = assertInstanceOf(AuthResponseDto.class, response.getBody());
        assertEquals("jwt-token", body.token());
        assertEquals("testuser", body.username());
    }

    @Test
    void signup_returnsBadRequestWhenRegistrationFails() {
        StubUserService userService = new StubUserService();
        StubJwtUtilsService jwtUtilsService = new StubJwtUtilsService();
        AuthController underTest = new AuthController(userService, jwtUtilsService);
        userService.registrationException = new IllegalArgumentException("Username already exists");

        ResponseEntity<?> response = underTest.signup(
                new SignupRequestDto("testuser", "password", "Test User", "test@example.com")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already exists", response.getBody());
    }

    @Test
    void login_returnsJwtWhenCredentialsAreValid() {
        StubUserService userService = new StubUserService();
        StubJwtUtilsService jwtUtilsService = new StubJwtUtilsService();
        AuthController underTest = new AuthController(userService, jwtUtilsService);
        User user = new User("testuser", "hashed-password", "Test User", "test@example.com");
        userService.authenticateUserResult = true;
        userService.userByUsername = user;
        jwtUtilsService.tokenToReturn = "jwt-token";

        ResponseEntity<?> response = underTest.login(new LoginRequestDto("testuser", "password"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponseDto body = assertInstanceOf(AuthResponseDto.class, response.getBody());
        assertEquals("jwt-token", body.token());
        assertEquals("testuser", body.username());
    }

    @Test
    void login_returnsUnauthorizedWhenCredentialsAreInvalid() {
        StubUserService userService = new StubUserService();
        StubJwtUtilsService jwtUtilsService = new StubJwtUtilsService();
        AuthController underTest = new AuthController(userService, jwtUtilsService);

        ResponseEntity<?> response = underTest.login(new LoginRequestDto("testuser", "wrong-password"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
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
        public String generateToken(org.springframework.security.core.userdetails.UserDetails userDetails) {
            return tokenToReturn;
        }
    }
}
