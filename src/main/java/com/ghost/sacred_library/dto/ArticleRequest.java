package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.enums.ContentFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ArticleRequest(
        @NotBlank @Size(max = 250) String title,
        @Size(max = 280) String slug,
        @Size(max = 600) String summary,
        @NotBlank String content,
        ContentFormat contentFormat,
        @Size(max = 500) String coverImageUrl,
        @NotNull Long categoryId,
        Boolean featured,
        @Size(max = 250) String metaTitle,
        @Size(max = 320) String metaDescription) {
}
