package com.ghost.sacred_library.controller;

import com.ghost.sacred_library.dto.TagRequest;
import com.ghost.sacred_library.dto.TagResponse;
import com.ghost.sacred_library.service.TagsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tags")
public class AdminTagsController {

    private final TagsService tagsService;

    public AdminTagsController(TagsService tagsService) {
        this.tagsService = tagsService;
    }

    @GetMapping
    public List<TagResponse> list() {
        return tagsService.findAllAdmin();
    }

    @GetMapping("/{id}")
    public TagResponse getById(@PathVariable Long id) {
        return tagsService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse create(@Valid @RequestBody TagRequest request) {
        return tagsService.create(request);
    }

    @PutMapping("/{id}")
    public TagResponse update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return tagsService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tagsService.delete(id);
    }
}
