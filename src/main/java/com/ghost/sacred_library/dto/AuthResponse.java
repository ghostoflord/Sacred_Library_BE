package com.ghost.sacred_library.dto;

public record AuthResponse(String tokenType, String token, long expiresIn, UserResponse user) { }
