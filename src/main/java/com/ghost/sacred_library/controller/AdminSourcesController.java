package com.ghost.sacred_library.controller;

import com.ghost.sacred_library.dto.ArticleSourceRequest;
import com.ghost.sacred_library.dto.ArticleSourceResponse;
import com.ghost.sacred_library.service.ArticlesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sources")
public class AdminSourcesController {

    private final ArticlesService articlesService;

    public AdminSourcesController(ArticlesService articlesService) {
        this.articlesService = articlesService;
    }

    @PutMapping("/{sourceId}")
    public ArticleSourceResponse update(
            @PathVariable Long sourceId,
            @Valid @RequestBody ArticleSourceRequest request) {
        return articlesService.updateSource(sourceId, request);
    }

    @DeleteMapping("/{sourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long sourceId) {
        articlesService.deleteSource(sourceId);
    }
}
