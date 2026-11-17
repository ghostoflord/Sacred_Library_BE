package com.ghost.sacred_library.controller;

import com.ghost.sacred_library.dto.ArticleDetailResponse;
import com.ghost.sacred_library.dto.ArticleListItemResponse;
import com.ghost.sacred_library.dto.ArticleSourceResponse;
import com.ghost.sacred_library.dto.PageResponse;
import com.ghost.sacred_library.service.ArticlesService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
public class ArticlesController {

    private final ArticlesService articlesService;

    public ArticlesController(ArticlesService articlesService) {
        this.articlesService = articlesService;
    }

    @GetMapping
    public PageResponse<ArticleListItemResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return articlesService.findPublished(q, categoryId, tag, page, pageSize, sortBy, sortDirection);
    }

    @GetMapping("/featured")
    public PageResponse<ArticleListItemResponse> featured(
            @RequestParam(defaultValue = "10") int limit) {
        return articlesService.findFeatured(limit);
    }

    @GetMapping("/recent")
    public PageResponse<ArticleListItemResponse> recent(
            @RequestParam(defaultValue = "10") int limit) {
        return articlesService.findRecent(limit);
    }

    @GetMapping("/{slug}/sources")
    public List<ArticleSourceResponse> sources(@PathVariable String slug) {
        return articlesService.findPublishedSourcesBySlug(slug);
    }

    @GetMapping("/{slug}")
    public ArticleDetailResponse getBySlug(@PathVariable String slug) {
        return articlesService.findPublishedBySlug(slug);
    }
}
