package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.entity.Categories;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        String slug,
        String categories_image_url) {

    public static CategoryResponse from(Categories category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSlug(),
                category.getCategories_image_url());
    }
}
