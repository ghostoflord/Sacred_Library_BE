package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.enums.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ArticleSourceRequest(
        @NotBlank @Size(max = 250) String title,
        @Size(max = 180) String creator,
        @NotNull SourceType sourceType,
        @Size(max = 1000) String url,
        @Size(max = 180) String publisher,
        Integer publishedYear,
        @Size(max = 100) String pageReference,
        Instant accessedAt,
        String note) {
}
