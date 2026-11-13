package com.ghost.sacred_library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 5000) String description,
        @Size(max = 180) String slug,
        @Size(max = 500) String categories_image_url) {
}
