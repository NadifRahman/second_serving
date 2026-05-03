package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.config.security.JwtUtilsService;
import com.secondserving.secondserving.config.security.UserDetailsImpl;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.dto.AuthResponseDto;
import com.secondserving.secondserving.dto.LoginRequestDto;
import com.secondserving.secondserving.dto.SignupRequestDto;
import com.secondserving.secondserving.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthController.AUTH_BASE_PATH)
public class AuthController {

    public static final String AUTH_BASE_PATH = ApiPaths.API_BASE_PATH;
    public static final String SIGNUP_PATH = "/signup";
    public static final String LOGIN_PATH = "/login";

    private final UserService userService;
    private final JwtUtilsService jwtUtilsService;

    public AuthController(UserService userService, JwtUtilsService jwtUtilsService) {
        this.userService = userService;
        this.jwtUtilsService = jwtUtilsService;
    }

    @PostMapping(SIGNUP_PATH)
    public ResponseEntity<AuthResponseDto> signup(@RequestBody SignupRequestDto signupRequestDto) {
        User user = userService.registerUser(
                signupRequestDto.username(),
                signupRequestDto.password(),
                signupRequestDto.fullName(),
                signupRequestDto.email()
        );

        UserDetails userDetails = new UserDetailsImpl(user);
        String token = jwtUtilsService.generateToken(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponseDto(token, user.getUsername()));
    }

    @PostMapping(LOGIN_PATH)
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        boolean authenticated = userService.authenticateUser(
                loginRequestDto.username(),
                loginRequestDto.password()
        );

        if (!authenticated) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userService.getUserByUsername(loginRequestDto.username());
        UserDetails userDetails = new UserDetailsImpl(user);
        String token = jwtUtilsService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponseDto(token, user.getUsername()));
    }
}
