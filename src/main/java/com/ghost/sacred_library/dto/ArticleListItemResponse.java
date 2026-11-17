package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.entity.Article;
import com.ghost.sacred_library.enums.ArticleStatus;
import java.time.Instant;

public record ArticleListItemResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String coverImageUrl,
        ArticleCategorySummary category,
        String authorName,
        ArticleStatus status,
        Instant publishedAt,
        Integer readingTimeMinutes,
        boolean featured,
        long viewCount,
        Instant createdAt,
        Instant updatedAt) {

    public static ArticleListItemResponse from(Article article) {
        return new ArticleListItemResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getCoverImageUrl(),
                ArticleCategorySummary.from(article.getCategory()),
                article.getAuthor().getUsername(),
                article.getStatus(),
                article.getPublishedAt(),
                article.getReadingTimeMinutes(),
                article.isFeatured(),
                article.getViewCount(),
                article.getCreatedAt(),
                article.getUpdatedAt());
    }
}
