package com.secondserving.secondserving.service;

import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service layer for user-related operations. This class will contain business logic for managing users, such as
 * registration, authentication, and profile management. It interacts with the UserRepository to perform database
 * operations and uses PasswordEncoder to securely handle passwords.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with the provided details. It checks for existing usernames and emails to prevent duplicates,
     * hashes the password before saving, and then creates and saves the new user in the database. If the username or email
     * already exists, it throws an IllegalArgumentException with an appropriate message.
     *
     * @param username
     * @param rawPassword
     * @param fullName
     * @param email
     * @return
     */
    public User registerUser(String username, String rawPassword, String fullName, String email) throws IllegalArgumentException {
        // Check if the username or email already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Create and save the new user
        User newUser = new User(username, hashedPassword, fullName, email);
        return userRepository.save(newUser);
    }

    /**
     * Authenticates a user by verifying the provided username and raw password against the stored hashed password in the database.
     * @param username
     * @param rawPassword
     * @return
     */
    public boolean authenticateUser(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()))
                .orElse(false);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Could not find user with username " + username));
    }
}
