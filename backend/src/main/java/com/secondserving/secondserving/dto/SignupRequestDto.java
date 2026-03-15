package com.secondserving.secondserving.dto;

public record SignupRequestDto(
        String username,
        String password,
        String fullName,
        String email
) {
}
