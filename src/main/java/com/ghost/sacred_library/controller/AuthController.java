package com.ghost.sacred_library.controller;

import com.ghost.sacred_library.dto.AuthResponse;
import com.ghost.sacred_library.dto.LoginRequest;
import com.ghost.sacred_library.dto.RegisterRequest;
import com.ghost.sacred_library.dto.UserResponse;
import com.ghost.sacred_library.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) { return authService.register(request); }
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }
}
