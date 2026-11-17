package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.entity.Article;
import com.ghost.sacred_library.enums.ArticleStatus;
import com.ghost.sacred_library.enums.ContentFormat;
import java.time.Instant;
import java.util.List;

public record ArticleDetailResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String content,
        ContentFormat contentFormat,
        String coverImageUrl,
        ArticleCategorySummary category,
        List<TagSummary> tags,
        List<ArticleSourceResponse> sources,
        Long authorId,
        String authorName,
        ArticleStatus status,
        Instant publishedAt,
        boolean featured,
        long viewCount,
        Integer readingTimeMinutes,
        String metaTitle,
        String metaDescription,
        Instant createdAt,
        Instant updatedAt) {

    public static ArticleDetailResponse from(Article article) {
        return new ArticleDetailResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getContent(),
                article.getContentFormat(),
                article.getCoverImageUrl(),
                ArticleCategorySummary.from(article.getCategory()),
                article.getTags().stream().map(TagSummary::from).toList(),
                article.getSources().stream().map(ArticleSourceResponse::from).toList(),
                article.getAuthor().getId(),
                article.getAuthor().getUsername(),
                article.getStatus(),
                article.getPublishedAt(),
                article.isFeatured(),
                article.getViewCount(),
                article.getReadingTimeMinutes(),
                article.getMetaTitle(),
                article.getMetaDescription(),
                article.getCreatedAt(),
                article.getUpdatedAt());
    }
}
