package com.secondserving.secondserving.service;

import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "hashedpassword", "Test User", "test@example.com");
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.registerUser("testuser", "password", "Test User", "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("hashedpassword", result.getPasswordHash());
        assertEquals("Test User", result.getFullName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void registerUser_UsernameExists_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            userService.registerUser("testuser", "password", "Test User", "test@example.com"));
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            userService.registerUser("testuser", "password", "Test User", "test@example.com"));
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(true);

        // Act
        boolean result = userService.authenticateUser("testuser", "password");

        // Assert
        assertTrue(result);
    }

    @Test
    void authenticateUser_InvalidUsername_ReturnsFalse() {
        // Arrange
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        // Act
        boolean result = userService.authenticateUser("invaliduser", "password");

        // Assert
        assertFalse(result);
    }

    @Test
    void authenticateUser_InvalidPassword_ReturnsFalse() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedpassword")).thenReturn(false);

        // Act
        boolean result = userService.authenticateUser("testuser", "wrongpassword");

        // Assert
        assertFalse(result);
    }
}
