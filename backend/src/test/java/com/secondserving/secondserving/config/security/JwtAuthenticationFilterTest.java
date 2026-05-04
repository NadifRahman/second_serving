package com.secondserving.secondserving.config.security;

import com.secondserving.secondserving.domain.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {

    private static final String TEST_SECRET = "Zm9yLXRlc3RzLW9ubHktdXNlLWEtbG9uZy1yYW5kb20tYmFzZTY0LXNlY3JldC12YWx1ZS1oZXJl";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_setsAuthenticationWhenBearerTokenIsValid() throws Exception {
        JwtUtilsService jwtUtilsService = new JwtUtilsService(TEST_SECRET, 3_600_000);
        UserDetails userDetails = new UserDetailsImpl(
                new User("alice", "hashed-password", "Alice Example", "alice@example.com")
        );
        UserDetailsService userDetailsService = username -> userDetails;
        JwtAuthenticationFilter underTest = new JwtAuthenticationFilter(jwtUtilsService, userDetailsService, "access_token");
        String token = jwtUtilsService.generateToken(userDetails);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        underTest.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        assertEquals("alice", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_setsAuthenticationWhenCookieTokenIsValid() throws Exception {
        JwtUtilsService jwtUtilsService = new JwtUtilsService(TEST_SECRET, 3_600_000);
        UserDetails userDetails = new UserDetailsImpl(
                new User("alice", "hashed-password", "Alice Example", "alice@example.com")
        );
        UserDetailsService userDetailsService = username -> userDetails;
        JwtAuthenticationFilter underTest = new JwtAuthenticationFilter(jwtUtilsService, userDetailsService, "access_token");
        String token = jwtUtilsService.generateToken(userDetails);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("access_token", token));

        underTest.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        assertEquals("alice", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_leavesContextEmptyWhenTokenIsMissing() throws Exception {
        JwtUtilsService jwtUtilsService = new JwtUtilsService(TEST_SECRET, 3_600_000);
        UserDetailsService userDetailsService = username -> {
            throw new UsernameNotFoundException(username);
        };
        JwtAuthenticationFilter underTest = new JwtAuthenticationFilter(jwtUtilsService, userDetailsService, "access_token");

        underTest.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_leavesContextEmptyWhenTokenIsInvalid() throws Exception {
        JwtUtilsService jwtUtilsService = new JwtUtilsService(TEST_SECRET, 3_600_000);
        UserDetailsService userDetailsService = username -> {
            throw new AssertionError("User details should not be loaded for an invalid token");
        };
        JwtAuthenticationFilter underTest = new JwtAuthenticationFilter(jwtUtilsService, userDetailsService, "access_token");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("access_token", "not-a-jwt"));

        underTest.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_leavesContextEmptyWhenTokenIsExpired() throws Exception {
        JwtUtilsService jwtUtilsService = new JwtUtilsService(TEST_SECRET, -1);
        UserDetails userDetails = new UserDetailsImpl(
                new User("alice", "hashed-password", "Alice Example", "alice@example.com")
        );
        JwtAuthenticationFilter underTest = new JwtAuthenticationFilter(
                jwtUtilsService,
                username -> userDetails,
                "access_token"
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("access_token", jwtUtilsService.generateToken(userDetails)));

        underTest.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
