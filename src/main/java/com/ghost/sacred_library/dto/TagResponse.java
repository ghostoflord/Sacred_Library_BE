package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.entity.Tag;
import java.time.Instant;

public record TagResponse(Long id, String name, String slug, String description, Instant createdAt) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getSlug(), tag.getDescription(), tag.getCreatedAt());
    }
}
