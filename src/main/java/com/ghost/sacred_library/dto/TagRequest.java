package com.ghost.sacred_library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 100) String slug,
        @Size(max = 255) String description) {
}
