package com.ghost.sacred_library.controller;

import com.ghost.sacred_library.dto.ArticleDetailResponse;
import com.ghost.sacred_library.dto.ArticleListItemResponse;
import com.ghost.sacred_library.dto.ArticleRequest;
import com.ghost.sacred_library.dto.PageResponse;
import com.ghost.sacred_library.enums.ArticleStatus;
import com.ghost.sacred_library.security.UserPrincipal;
import com.ghost.sacred_library.service.ArticlesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticlesController {

    private final ArticlesService articlesService;

    public AdminArticlesController(ArticlesService articlesService) {
        this.articlesService = articlesService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleDetailResponse create(
            @Valid @RequestBody ArticleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return articlesService.create(request, principal.getId());
    }

    @GetMapping
    public PageResponse<ArticleListItemResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ArticleStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return articlesService.findAdmin(q, status, categoryId, authorId, page, pageSize, sortBy, sortDirection);
    }

    @GetMapping("/{id}")
    public ArticleDetailResponse getById(@PathVariable Long id) {
        return articlesService.findAdminById(id);
    }

    @PutMapping("/{id}")
    public ArticleDetailResponse update(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return articlesService.update(id, request);
    }

    @PostMapping("/{id}/publish")
    public ArticleDetailResponse publish(@PathVariable Long id) {
        return articlesService.publish(id);
    }

    @PostMapping("/{id}/unpublish")
    public ArticleDetailResponse unpublish(@PathVariable Long id) {
        return articlesService.unpublish(id);
    }

    @PostMapping("/{id}/archive")
    public ArticleDetailResponse archive(@PathVariable Long id) {
        return articlesService.archive(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        articlesService.softDelete(id);
    }
}
