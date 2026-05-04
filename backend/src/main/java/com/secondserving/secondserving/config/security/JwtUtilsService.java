package com.secondserving.secondserving.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility service for issuing and validating signed JWT access tokens.
 */
@Service
public class JwtUtilsService {

    private final SecretKey signingKey;
    private final long jwtExpirationMs;

    public JwtUtilsService(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Extracts the username stored as the JWT subject.
     *
     * @param token signed JWT to inspect
     * @return the username stored in the subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a signed JWT for the supplied user.
     * <p>
     * This compatibility method returns only the raw token. New authentication
     * responses should prefer {@link #generateTokenWithExpiration(UserDetails)}
     * so they can return matching session expiry metadata.
     *
     * @param userDetails authenticated user details used to populate the JWT subject
     * @return signed JWT string
     */
    public String generateToken(UserDetails userDetails) {
        return generateTokenWithExpiration(userDetails).token();
    }

    /**
     * Generates a signed JWT and its exact expiration instant.
     *
     * @param userDetails authenticated user details used to populate the JWT subject
     * @return generated token plus the instant at which it expires
     */
    public GeneratedJwt generateTokenWithExpiration(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    private GeneratedJwt generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtExpirationMs);

        String token = Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // subject of JWT is the username
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new GeneratedJwt(token, expiresAt);
    }

    /**
     * Validates that a JWT belongs to the supplied user and has not expired.
     *
     * @param token signed JWT to validate
     * @param userDetails expected user details for the token subject
     * @return {@code true} when the token subject matches the user and the token is still valid
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks whether a token has passed its expiration timestamp.
     *
     * @param token signed JWT to inspect
     * @return {@code true} when the token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Generated JWT value together with the expiration instant used in the token claims.
     *
     * @param token signed JWT string
     * @param expiresAt instant when the token expires
     */
    public record GeneratedJwt(
            String token,
            Instant expiresAt
    ) {
    }
}
