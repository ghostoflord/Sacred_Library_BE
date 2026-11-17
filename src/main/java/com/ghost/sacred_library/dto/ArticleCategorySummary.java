package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.entity.Categories;

public record ArticleCategorySummary(Long id, String name, String slug) {

    public static ArticleCategorySummary from(Categories category) {
        return new ArticleCategorySummary(category.getId(), category.getName(), category.getSlug());
    }
}
