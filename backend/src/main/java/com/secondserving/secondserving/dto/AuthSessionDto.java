package com.secondserving.secondserving.dto;

import java.time.Instant;

/**
 * Response DTO returned after a successful authentication action.
 * <p>
 * The JWT itself is intentionally not included here. The backend sends the token
 * separately as an HttpOnly cookie, while this DTO gives the frontend enough
 * session metadata to update its UI immediately.
 *
 * @param user the authenticated user's safe profile fields
 * @param expiresAt the instant when the issued JWT expires
 */
public record AuthSessionDto(
        CurrentUserDto user,
        Instant expiresAt
) {
}
