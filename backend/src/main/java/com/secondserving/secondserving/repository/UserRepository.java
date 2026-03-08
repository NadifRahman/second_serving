package com.secondserving.secondserving.repository;

import com.secondserving.secondserving.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Fetch a {@link User} by its username
     * @param username The username of the User
     * @return
     */
    Optional<User> findByUsername(String username);

    /**
     * Fetch a {@link User} by its username
     * @param email The email of the User
     * @return
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a {@link User} exists with a given username
     */
    boolean existsByUsername(String username);

    /**
     * Check if a {@link User} exists with a given email
     */
    boolean existsByEmail(String email);
}