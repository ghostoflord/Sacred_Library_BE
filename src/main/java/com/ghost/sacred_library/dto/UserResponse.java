package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.entity.User;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(Long id, String username, String email, Set<String> roles, boolean enabled) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toUnmodifiableSet()), user.isEnabled());
    }
}
