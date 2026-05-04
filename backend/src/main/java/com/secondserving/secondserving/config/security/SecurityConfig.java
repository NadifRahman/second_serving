package com.secondserving.secondserving.config.security;

import com.secondserving.secondserving.controller.UserController;
import com.secondserving.secondserving.controller.AuthController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.secondserving.secondserving.config.security.UserDetailsImpl.ROLE_USER;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Route authorization config
                .authorizeHttpRequests((request) -> request
                // TODO: See if we should restrict these endpoints for prod, and only allow for dev.
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(AuthController.AUTH_BASE_PATH + AuthController.LOGIN_PATH).permitAll()
                .requestMatchers(AuthController.AUTH_BASE_PATH + AuthController.SIGNUP_PATH).permitAll()
                .requestMatchers(AuthController.AUTH_BASE_PATH + AuthController.LOGOUT_PATH).permitAll()
                .requestMatchers(UserController.USER_BASE_PATH + "/**").hasAuthority(ROLE_USER)
                .anyRequest().authenticated())

                // We dont want the default spring login form, or basic logic, or csrf since its better for JWTs
                .formLogin(form -> form.disable())
                .httpBasic(basicAuth -> basicAuth.disable())
                .csrf(csrf -> csrf.disable()) // disabling this to work with JWTs better
                // Stateless sessions because we will use token based auth (JWTs)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Setup our custom JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
