package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.config.security.JwtUtilsService;
import com.secondserving.secondserving.config.security.UserDetailsImpl;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.dto.AuthSessionDto;
import com.secondserving.secondserving.dto.CurrentUserDto;
import com.secondserving.secondserving.dto.LoginRequestDto;
import com.secondserving.secondserving.dto.SignupRequestDto;
import com.secondserving.secondserving.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Handles public authentication workflows.
 * <p>
 * Login and signup return safe session metadata in the response body, but the JWT
 * is delivered through an HttpOnly cookie so browser JavaScript does not need to
 * read or store the raw token.
 */
@RestController
@RequestMapping(AuthController.AUTH_BASE_PATH)
public class AuthController {

    public static final String AUTH_BASE_PATH = ApiPaths.API_BASE_PATH;
    public static final String SIGNUP_PATH = "/signup";
    public static final String LOGIN_PATH = "/login";
    public static final String LOGOUT_PATH = "/logout";

    private final UserService userService;
    private final JwtUtilsService jwtUtilsService;
    private final String jwtCookieName;
    private final boolean jwtCookieSecure;
    private final String jwtCookieSameSite;
    private final long jwtExpirationMs;

    /**
     * Creates the auth controller with JWT cookie settings supplied from application properties.
     *
     * @param userService service used for registration and credential checks
     * @param jwtUtilsService service used to issue signed JWTs
     * @param jwtCookieName name of the cookie that stores the JWT
     * @param jwtCookieSecure whether the cookie should only be sent over HTTPS
     * @param jwtCookieSameSite SameSite policy to apply to the JWT cookie
     * @param jwtExpirationMs JWT lifetime in milliseconds; the auth cookie uses the same duration for Max-Age so the
     *                        browser-held cookie expires at roughly the same time as the JWT inside it
     */
    public AuthController(
            UserService userService,
            JwtUtilsService jwtUtilsService,
            @Value("${app.jwt.cookie-name}") String jwtCookieName,
            @Value("${app.jwt.cookie-secure}") boolean jwtCookieSecure,
            @Value("${app.jwt.cookie-same-site}") String jwtCookieSameSite,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs
    ) {
        this.userService = userService;
        this.jwtUtilsService = jwtUtilsService;
        this.jwtCookieName = jwtCookieName;
        this.jwtCookieSecure = jwtCookieSecure;
        this.jwtCookieSameSite = jwtCookieSameSite;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Registers a user, issues a JWT cookie, and returns the new authenticated session.
     *
     * @param signupRequestDto registration fields submitted by the client
     * @return a created response containing safe user/session metadata and a Set-Cookie header
     */
    @PostMapping(SIGNUP_PATH)
    public ResponseEntity<AuthSessionDto> signup(@RequestBody SignupRequestDto signupRequestDto) {
        User user = userService.registerUser(
                signupRequestDto.username(),
                signupRequestDto.password(),
                signupRequestDto.fullName(),
                signupRequestDto.email()
        );

        UserDetails userDetails = new UserDetailsImpl(user);
        JwtUtilsService.GeneratedJwt generatedJwt = jwtUtilsService.generateTokenWithExpiration(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, buildAuthCookie(generatedJwt.token()).toString())
                .body(new AuthSessionDto(CurrentUserDto.from(user), generatedJwt.expiresAt()));
    }

    /**
     * Authenticates a user, issues a JWT cookie, and returns the authenticated session.
     *
     * @param loginRequestDto username and password submitted by the client
     * @return an OK response containing safe user/session metadata and a Set-Cookie header
     * @throws BadCredentialsException when the username/password pair is invalid
     */
    @PostMapping(LOGIN_PATH)
    public ResponseEntity<AuthSessionDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        boolean authenticated = userService.authenticateUser(
                loginRequestDto.username(),
                loginRequestDto.password()
        );

        if (!authenticated) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userService.getUserByUsername(loginRequestDto.username());
        UserDetails userDetails = new UserDetailsImpl(user);
        JwtUtilsService.GeneratedJwt generatedJwt = jwtUtilsService.generateTokenWithExpiration(userDetails);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildAuthCookie(generatedJwt.token()).toString())
                .body(new AuthSessionDto(CurrentUserDto.from(user), generatedJwt.expiresAt()));
    }

    /**
     * Clears the browser-held JWT cookie.
     * <p>
     * There is no server-side session to invalidate; logout is represented by
     * expiring the auth cookie on the client. Cookies are removed by sending a
     * {@code Set-Cookie} header with the same cookie name and path plus {@code Max-Age=0};
     * browsers then overwrite the existing cookie and immediately expire it.
     *
     * @return a no-content response with a Set-Cookie header that removes the JWT cookie
     */
    @PostMapping(LOGOUT_PATH)
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from(jwtCookieName, "")
                .httpOnly(true)
                .secure(jwtCookieSecure)
                .sameSite(jwtCookieSameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * Builds the HttpOnly JWT cookie used by login and signup.
     * <p>
     * The JWT's expiration claim and the cookie's expiration are separate controls:
     * the JWT decides whether the backend will accept the token, while the cookie
     * {@code Max-Age} decides how long the browser will keep sending it. We set the
     * cookie Max-Age from the same {@code app.jwt.expiration-ms} value used to sign
     * the JWT so the browser stops sending the token around the same time the backend
     * would reject it.
     *
     * @param token the signed JWT to store in the browser-managed cookie jar
     * @return the configured auth cookie
     */
    private ResponseCookie buildAuthCookie(String token) {
        return ResponseCookie.from(jwtCookieName, token)
                .httpOnly(true)
                .secure(jwtCookieSecure)
                .sameSite(jwtCookieSameSite)
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpirationMs))
                .build();
    }
}
