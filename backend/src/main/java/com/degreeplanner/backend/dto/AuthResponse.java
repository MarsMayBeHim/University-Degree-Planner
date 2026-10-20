package com.degreeplanner.backend.dto;

public record AuthResponse(String token, UserDto user) {}