package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.entity.ArticleSource;
import com.ghost.sacred_library.enums.SourceType;
import java.time.Instant;

public record ArticleSourceResponse(
        Long id,
        Long articleId,
        String title,
        String creator,
        SourceType sourceType,
        String url,
        String publisher,
        Integer publishedYear,
        String pageReference,
        Instant accessedAt,
        String note,
        Instant createdAt) {

    public static ArticleSourceResponse from(ArticleSource source) {
        return new ArticleSourceResponse(
                source.getId(),
                source.getArticle().getId(),
                source.getTitle(),
                source.getCreator(),
                source.getSourceType(),
                source.getUrl(),
                source.getPublisher(),
                source.getPublishedYear(),
                source.getPageReference(),
                source.getAccessedAt(),
                source.getNote(),
                source.getCreatedAt());
    }
}
