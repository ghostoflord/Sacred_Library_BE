package com.ghost.sacred_library.service;

import com.ghost.sacred_library.dto.ArticleListItemResponse;
import com.ghost.sacred_library.dto.PageResponse;
import com.ghost.sacred_library.dto.TagRequest;
import com.ghost.sacred_library.dto.TagResponse;
import com.ghost.sacred_library.entity.Tag;
import com.ghost.sacred_library.repository.ArticleRepository;
import com.ghost.sacred_library.repository.TagRepository;
import com.ghost.sacred_library.util.SlugUtils;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagsService {

    private final TagRepository tagRepository;
    private final ArticleRepository articleRepository;
    private final AuditLogService auditLogService;

    public TagsService(
            TagRepository tagRepository,
            ArticleRepository articleRepository,
            AuditLogService auditLogService) {
        this.tagRepository = tagRepository;
        this.articleRepository = articleRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> findPublic() {
        return tagRepository.findUsedByPublishedArticles().stream().map(TagResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TagResponse> findAllAdmin() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(TagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TagResponse findById(Long id) {
        return TagResponse.from(getTag(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleListItemResponse> findPublishedArticlesBySlug(
            String slug, int page, int pageSize) {
        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found."));
        int safePage = Math.max(page, 1) - 1;
        int safeSize = pageSize <= 0 ? 20 : Math.min(pageSize, 100);
        var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return PageResponse.from(
                articleRepository
                        .searchPublished(null, null, tag.getSlug(), Instant.now(), pageable)
                        .map(ArticleListItemResponse::from));
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        String name = request.name().trim();
        String slug = SlugUtils.resolve(request.slug(), name);
        ensureUnique(name, slug, null);
        Tag tag = tagRepository.save(new Tag(name, slug, SlugUtils.normalizeNullable(request.description())));
        auditLogService.record("TAG_CREATED", "TAG", tag.getId(), Map.of("slug", tag.getSlug()));
        return TagResponse.from(tag);
    }

    @Transactional
    public TagResponse update(Long id, TagRequest request) {
        Tag tag = getTag(id);
        String name = request.name().trim();
        String slug = SlugUtils.resolve(request.slug(), name);
        ensureUnique(name, slug, id);
        tag.setName(name);
        tag.setSlug(slug);
        tag.setDescription(SlugUtils.normalizeNullable(request.description()));
        Tag saved = tagRepository.save(tag);
        auditLogService.record("TAG_UPDATED", "TAG", saved.getId(), Map.of("slug", saved.getSlug()));
        return TagResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = getTag(id);
        tagRepository.deleteArticleLinks(id);
        tagRepository.delete(tag);
        auditLogService.record("TAG_DELETED", "TAG", id, Map.of("slug", tag.getSlug()));
    }

    private Tag getTag(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found."));
    }

    private void ensureUnique(String name, String slug, Long currentId) {
        boolean nameTaken = currentId == null
                ? tagRepository.existsByNameIgnoreCase(name)
                : tagRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (nameTaken) {
            throw new DuplicateSlugException("Tag name is already in use.");
        }
        boolean slugTaken = currentId == null
                ? tagRepository.existsBySlug(slug)
                : tagRepository.existsBySlugAndIdNot(slug, currentId);
        if (slugTaken) {
            throw new DuplicateSlugException("Slug is already in use.");
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateSlugException extends RuntimeException {
        public DuplicateSlugException(String message) {
            super(message);
        }
    }
}
