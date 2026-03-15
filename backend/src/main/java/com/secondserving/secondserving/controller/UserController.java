package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping(UserController.USER_BASE_PATH)
public class UserController {

    public static final String USER_BASE_PATH = "/user";

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    // TODO just a test endpoint...delete later
    public ResponseEntity<String> getUser(Authentication authentication) {
        authentication.getName();
        return ResponseEntity.status(HttpStatus.OK).body(authentication.getName());
    }
}