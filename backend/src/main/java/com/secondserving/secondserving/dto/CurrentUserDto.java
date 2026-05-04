package com.secondserving.secondserving.dto;

import com.secondserving.secondserving.domain.User;

import java.util.UUID;

/**
 * Safe representation of the authenticated user that can be returned to clients.
 * <p>
 * This DTO deliberately excludes sensitive fields such as the password hash.
 *
 * @param userId the user's stable identifier
 * @param username the user's public username
 * @param fullName the user's display name
 * @param email the user's email address
 */
public record CurrentUserDto(
        UUID userId,
        String username,
        String fullName,
        String email
) {
    /**
     * Builds a client-safe user DTO from the persisted user entity.
     *
     * @param user the domain user to expose to the authenticated client
     * @return the safe current-user DTO
     */
    public static CurrentUserDto from(User user) {
        return new CurrentUserDto(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail()
        );
    }
}
