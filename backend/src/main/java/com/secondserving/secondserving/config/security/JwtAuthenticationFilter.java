package com.secondserving.secondserving.config.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Authenticates requests that present a valid JWT.
 * <p>
 * The filter accepts the legacy {@code Authorization: Bearer ...} header for
 * Swagger and development workflows, and also accepts the configured HttpOnly
 * cookie used by the browser frontend. Invalid, expired, or malformed JWTs are
 * ignored so Spring Security can reject protected endpoints as unauthenticated.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtilsService jwtUtilsService;
    private final UserDetailsService userDetailsService;
    private final String jwtCookieName;

    /**
     * Creates the JWT authentication filter.
     *
     * @param jwtUtilsService utility service for parsing and validating JWTs
     * @param userDetailsService service used to load the user referenced by a valid JWT subject
     * @param jwtCookieName configured cookie name that may contain the JWT
     */
    public JwtAuthenticationFilter(
            JwtUtilsService jwtUtilsService,
            UserDetailsService userDetailsService,
            @Value("${app.jwt.cookie-name}") String jwtCookieName
    ) {
        this.jwtUtilsService = jwtUtilsService;
        this.userDetailsService = userDetailsService;
        this.jwtCookieName = jwtCookieName;
    }

    /**
     * Resolves a JWT from the request, validates it, and populates the Spring Security context.
     * <p>
     * If the token is absent or invalid, the filter leaves the security context untouched and
     * continues the chain.
     *
     * @param request incoming HTTP request
     * @param response outgoing HTTP response
     * @param filterChain remaining servlet filter chain
     * @throws ServletException if the downstream filter chain fails
     * @throws IOException if the downstream filter chain fails during I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = resolveJwt(request);

        if (jwt == null) {
            // Do nothing...go to the next filter
            filterChain.doFilter(request, response);
            return;
        }

        String username;
        try {
            username = jwtUtilsService.extractUsername(jwt);
        } catch (JwtException | IllegalArgumentException e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtilsService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            // Set the principal here, cred can be null we dont care
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Set the Authentican in the SecurityContext for use in
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                // TODO add a log here
            }
        }
        filterChain.doFilter(request, response);

    }

    /**
     * Finds the JWT supplied with the request.
     * <p>
     * Bearer headers are checked first so Swagger UI and command-line clients keep working.
     * If no bearer token is present, the configured auth cookie is used.
     *
     * @param request incoming HTTP request
     * @return the raw JWT, or {@code null} when no token is present
     */
    private String resolveJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> jwtCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
