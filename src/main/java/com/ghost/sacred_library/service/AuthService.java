package com.ghost.sacred_library.service;

import com.ghost.sacred_library.dto.AuthResponse;
import com.ghost.sacred_library.dto.LoginRequest;
import com.ghost.sacred_library.dto.RegisterRequest;
import com.ghost.sacred_library.dto.UserResponse;
import com.ghost.sacred_library.entity.Role;
import com.ghost.sacred_library.entity.User;
import com.ghost.sacred_library.repository.RoleRepository;
import com.ghost.sacred_library.repository.UserRepository;
import com.ghost.sacred_library.security.JwtService;
import com.ghost.sacred_library.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    public AuthService(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users; this.roles = roles; this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager; this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes.");
        if (users.existsByUsername(username)) throw new DuplicateAccountException("Username is already in use.");
        if (users.existsByEmail(email)) throw new DuplicateAccountException("Email is already in use.");
        Role userRole = roles.findByName("ROLE_USER").orElseThrow(() -> new IllegalStateException("ROLE_USER is not initialized."));
        User user = new User(username, email, passwordEncoder.encode(request.password()));
        user.addRole(userRole); // Role is selected on the server, never from registration JSON.
        return UserResponse.from(users.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String login = request.usernameOrEmail().trim();
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return new AuthResponse("Bearer", jwtService.createToken(principal.getUser()), jwtService.getExpirationMs(), UserResponse.from(principal.getUser()));
    }

    public static class DuplicateAccountException extends RuntimeException {
        public DuplicateAccountException(String message) { super(message); }
    }
}
