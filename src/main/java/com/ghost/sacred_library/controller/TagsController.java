package com.ghost.sacred_library.controller;

import com.ghost.sacred_library.dto.ArticleListItemResponse;
import com.ghost.sacred_library.dto.PageResponse;
import com.ghost.sacred_library.dto.TagResponse;
import com.ghost.sacred_library.service.TagsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
public class TagsController {

    private final TagsService tagsService;

    public TagsController(TagsService tagsService) {
        this.tagsService = tagsService;
    }

    @GetMapping
    public List<TagResponse> list() {
        return tagsService.findPublic();
    }

    @GetMapping("/{slug}/articles")
    public PageResponse<ArticleListItemResponse> articlesByTag(
            @PathVariable String slug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return tagsService.findPublishedArticlesBySlug(slug, page, pageSize);
    }
}
