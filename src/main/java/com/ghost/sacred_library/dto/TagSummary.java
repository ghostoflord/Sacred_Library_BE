package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.entity.Tag;

public record TagSummary(Long id, String name, String slug) {

    public static TagSummary from(Tag tag) {
        return new TagSummary(tag.getId(), tag.getName(), tag.getSlug());
    }
}
