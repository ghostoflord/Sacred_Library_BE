package com.ghost.sacred_library.service;

import com.ghost.sacred_library.dto.ArticleDetailResponse;
import com.ghost.sacred_library.dto.ArticleListItemResponse;
import com.ghost.sacred_library.dto.ArticleRequest;
import com.ghost.sacred_library.dto.ArticleSourceRequest;
import com.ghost.sacred_library.dto.ArticleSourceResponse;
import com.ghost.sacred_library.dto.PageResponse;
import com.ghost.sacred_library.entity.Article;
import com.ghost.sacred_library.entity.ArticleSource;
import com.ghost.sacred_library.entity.Categories;
import com.ghost.sacred_library.entity.Tag;
import com.ghost.sacred_library.entity.User;
import com.ghost.sacred_library.enums.ArticleStatus;
import com.ghost.sacred_library.enums.ContentFormat;
import com.ghost.sacred_library.repository.ArticleRepository;
import com.ghost.sacred_library.repository.ArticleSourceRepository;
import com.ghost.sacred_library.repository.CategoriesRepository;
import com.ghost.sacred_library.repository.TagRepository;
import com.ghost.sacred_library.repository.UserRepository;
import com.ghost.sacred_library.util.SlugUtils;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticlesService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int WORDS_PER_MINUTE = 200;

    private final ArticleRepository articleRepository;
    private final CategoriesRepository categoriesRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ArticleSourceRepository articleSourceRepository;
    private final AuditLogService auditLogService;

    public ArticlesService(
            ArticleRepository articleRepository,
            CategoriesRepository categoriesRepository,
            UserRepository userRepository,
            TagRepository tagRepository,
            ArticleSourceRepository articleSourceRepository,
            AuditLogService auditLogService) {
        this.articleRepository = articleRepository;
        this.categoriesRepository = categoriesRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.articleSourceRepository = articleSourceRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ArticleDetailResponse create(ArticleRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found."));
        Categories category = getCategory(request.categoryId());
        String title = request.title().trim();
        String content = request.content().trim();
        String slug = SlugUtils.resolve(request.slug(), title);
        ensureSlugAvailable(slug, null);

        Article article = new Article(title, slug, content, category, author);
        applyWritableFields(article, request, title, content);
        article.setStatus(ArticleStatus.DRAFT);
        article.setPublishedAt(null);
        applyTags(article, request.tagIds());
        applySources(article, request.sources());

        Article saved = articleRepository.save(article);
        auditLogService.record("ARTICLE_CREATED", "ARTICLE", saved.getId(), Map.of("slug", saved.getSlug()));
        return ArticleDetailResponse.from(saved);
    }

    @Transactional
    public ArticleDetailResponse update(Long id, ArticleRequest request) {
        Article article = getActiveArticle(id);
        Categories category = getCategory(request.categoryId());
        String title = request.title().trim();
        String content = request.content().trim();
        String slug = SlugUtils.resolve(request.slug(), title);
        ensureSlugAvailable(slug, id);

        article.setTitle(title);
        article.setSlug(slug);
        article.setContent(content);
        article.setCategory(category);
        applyWritableFields(article, request, title, content);
        applyTags(article, request.tagIds());
        if (request.sources() != null) {
            applySources(article, request.sources());
        }

        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            validatePublishable(article);
            if (article.getPublishedAt() == null) {
                article.setPublishedAt(Instant.now());
            }
        }

        Article saved = articleRepository.save(article);
        auditLogService.record("ARTICLE_UPDATED", "ARTICLE", saved.getId(), Map.of("slug", saved.getSlug()));
        return ArticleDetailResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ArticleDetailResponse findAdminById(Long id) {
        return ArticleDetailResponse.from(getActiveArticle(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleListItemResponse> findAdmin(
            String q,
            ArticleStatus status,
            Long categoryId,
            Long authorId,
            int page,
            int pageSize,
            String sortBy,
            String sortDirection) {
        Pageable pageable = toPageable(page, pageSize, sortBy, sortDirection, "updatedAt");
        Page<ArticleListItemResponse> result = articleRepository
                .searchAdmin(normalizeQuery(q), status, categoryId, authorId, pageable)
                .map(ArticleListItemResponse::from);
        return PageResponse.from(result);
    }

    @Transactional
    public ArticleDetailResponse publish(Long id) {
        Article article = getActiveArticle(id);
        validatePublishable(article);
        article.setStatus(ArticleStatus.PUBLISHED);
        if (article.getPublishedAt() == null) {
            article.setPublishedAt(Instant.now());
        }
        Article saved = articleRepository.save(article);
        auditLogService.record("ARTICLE_PUBLISHED", "ARTICLE", saved.getId(), Map.of("slug", saved.getSlug()));
        return ArticleDetailResponse.from(saved);
    }

    @Transactional
    public ArticleDetailResponse unpublish(Long id) {
        Article article = getActiveArticle(id);
        article.setStatus(ArticleStatus.DRAFT);
        Article saved = articleRepository.save(article);
        auditLogService.record("ARTICLE_UNPUBLISHED", "ARTICLE", saved.getId(), Map.of("slug", saved.getSlug()));
        return ArticleDetailResponse.from(saved);
    }

    @Transactional
    public ArticleDetailResponse archive(Long id) {
        Article article = getActiveArticle(id);
        article.setStatus(ArticleStatus.ARCHIVED);
        Article saved = articleRepository.save(article);
        auditLogService.record("ARTICLE_ARCHIVED", "ARTICLE", saved.getId(), Map.of("slug", saved.getSlug()));
        return ArticleDetailResponse.from(saved);
    }

    @Transactional
    public void softDelete(Long id) {
        Article article = getActiveArticle(id);
        article.softDelete();
        articleRepository.save(article);
        auditLogService.record("ARTICLE_DELETED", "ARTICLE", id, Map.of("slug", article.getSlug()));
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleListItemResponse> findPublished(
            String q,
            Long categoryId,
            String tag,
            int page,
            int pageSize,
            String sortBy,
            String sortDirection) {
        Pageable pageable = toPageable(page, pageSize, sortBy, sortDirection, "publishedAt");
        Page<ArticleListItemResponse> result = articleRepository
                .searchPublished(normalizeQuery(q), categoryId, normalizeQuery(tag), Instant.now(), pageable)
                .map(ArticleListItemResponse::from);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleListItemResponse> findFeatured(int limit) {
        int size = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, size);
        Page<ArticleListItemResponse> result = articleRepository
                .findFeaturedPublished(Instant.now(), pageable)
                .map(ArticleListItemResponse::from);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleListItemResponse> findRecent(int limit) {
        int size = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<ArticleListItemResponse> result = articleRepository
                .searchPublished(null, null, null, Instant.now(), pageable)
                .map(ArticleListItemResponse::from);
        return PageResponse.from(result);
    }

    @Transactional
    public ArticleDetailResponse findPublishedBySlug(String slug) {
        Article article = articleRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found."));
        if (article.getStatus() != ArticleStatus.PUBLISHED
                || article.getPublishedAt() == null
                || article.getPublishedAt().isAfter(Instant.now())) {
            throw new ResourceNotFoundException("Article not found.");
        }
        article.incrementViewCount();
        return ArticleDetailResponse.from(articleRepository.save(article));
    }

    @Transactional(readOnly = true)
    public List<ArticleSourceResponse> findPublishedSourcesBySlug(String slug) {
        Article article = articleRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found."));
        if (article.getStatus() != ArticleStatus.PUBLISHED
                || article.getPublishedAt() == null
                || article.getPublishedAt().isAfter(Instant.now())) {
            throw new ResourceNotFoundException("Article not found.");
        }
        return article.getSources().stream().map(ArticleSourceResponse::from).toList();
    }

    @Transactional
    public ArticleSourceResponse addSource(Long articleId, ArticleSourceRequest request) {
        Article article = getActiveArticle(articleId);
        ArticleSource source = toSourceEntity(article, request);
        article.addSource(source);
        articleRepository.save(article);
        auditLogService.record("ARTICLE_SOURCE_CREATED", "ARTICLE_SOURCE", source.getId(),
                Map.of("articleId", articleId));
        return ArticleSourceResponse.from(source);
    }

    @Transactional
    public ArticleSourceResponse updateSource(Long sourceId, ArticleSourceRequest request) {
        ArticleSource source = articleSourceRepository.findByIdAndArticleDeletedAtIsNull(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found."));
        applySourceFields(source, request);
        ArticleSource saved = articleSourceRepository.save(source);
        auditLogService.record("ARTICLE_SOURCE_UPDATED", "ARTICLE_SOURCE", saved.getId(),
                Map.of("articleId", saved.getArticle().getId()));
        return ArticleSourceResponse.from(saved);
    }

    @Transactional
    public void deleteSource(Long sourceId) {
        ArticleSource source = articleSourceRepository.findByIdAndArticleDeletedAtIsNull(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found."));
        Article article = source.getArticle();
        Long articleId = article.getId();
        article.removeSource(source);
        articleRepository.save(article);
        auditLogService.record("ARTICLE_SOURCE_DELETED", "ARTICLE_SOURCE", sourceId,
                Map.of("articleId", articleId));
    }

    private void applyWritableFields(Article article, ArticleRequest request, String title, String content) {
        article.setSummary(SlugUtils.normalizeNullable(request.summary()));
        article.setContentFormat(request.contentFormat() == null ? ContentFormat.MARKDOWN : request.contentFormat());
        article.setCoverImageUrl(SlugUtils.normalizeNullable(request.coverImageUrl()));
        article.setFeatured(Boolean.TRUE.equals(request.featured()));
        article.setMetaTitle(SlugUtils.normalizeNullable(request.metaTitle()));
        article.setMetaDescription(SlugUtils.normalizeNullable(request.metaDescription()));
        article.setReadingTimeMinutes(estimateReadingTime(content));
        if (article.getMetaTitle() == null) {
            article.setMetaTitle(title);
        }
    }

    private void applyTags(Article article, List<Long> tagIds) {
        if (tagIds == null) {
            return;
        }
        if (tagIds.isEmpty()) {
            article.setTags(Set.of());
            return;
        }
        List<Tag> tags = tagRepository.findByIdIn(tagIds);
        if (tags.size() != tagIds.stream().distinct().count()) {
            throw new ResourceNotFoundException("One or more tags were not found.");
        }
        article.setTags(new HashSet<>(tags));
    }

    private void applySources(Article article, List<ArticleSourceRequest> requests) {
        if (requests == null) {
            article.replaceSources(List.of());
            return;
        }
        List<ArticleSource> sources = requests.stream()
                .map(req -> toSourceEntity(article, req))
                .toList();
        article.replaceSources(sources);
    }

    private ArticleSource toSourceEntity(Article article, ArticleSourceRequest request) {
        ArticleSource source = new ArticleSource(article, request.title().trim(), request.sourceType());
        applySourceFields(source, request);
        return source;
    }

    private void applySourceFields(ArticleSource source, ArticleSourceRequest request) {
        source.setTitle(request.title().trim());
        source.setCreator(SlugUtils.normalizeNullable(request.creator()));
        source.setSourceType(request.sourceType());
        source.setUrl(SlugUtils.normalizeNullable(request.url()));
        source.setPublisher(SlugUtils.normalizeNullable(request.publisher()));
        source.setPublishedYear(request.publishedYear());
        source.setPageReference(SlugUtils.normalizeNullable(request.pageReference()));
        source.setAccessedAt(request.accessedAt());
        source.setNote(SlugUtils.normalizeNullable(request.note()));
    }

    private void validatePublishable(Article article) {
        if (article.getTitle() == null || article.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required to publish.");
        }
        if (article.getContent() == null || article.getContent().isBlank()) {
            throw new IllegalArgumentException("Content is required to publish.");
        }
        if (article.getCategory() == null) {
            throw new IllegalArgumentException("Category is required to publish.");
        }
    }

    private Article getActiveArticle(Long id) {
        return articleRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found."));
    }

    private Categories getCategory(Long categoryId) {
        return categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }

    private void ensureSlugAvailable(String slug, Long currentId) {
        boolean taken = currentId == null
                ? articleRepository.existsBySlugAndDeletedAtIsNull(slug)
                : articleRepository.existsBySlugAndIdNotAndDeletedAtIsNull(slug, currentId);
        if (taken) {
            throw new DuplicateSlugException("Slug is already in use.");
        }
    }

    private static String normalizeQuery(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return q.trim();
    }

    private static int estimateReadingTime(String content) {
        String plain = content.replaceAll("[#*_>`\\[\\]()!-]", " ").trim();
        if (plain.isEmpty()) {
            return 1;
        }
        int words = plain.split("\\s+").length;
        return Math.max(1, (int) Math.ceil(words / (double) WORDS_PER_MINUTE));
    }

    private static Pageable toPageable(int page, int pageSize, String sortBy, String sortDirection, String defaultSort) {
        int safePage = Math.max(page, 1) - 1;
        int safeSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        String property = switch (sortBy == null ? "" : sortBy.trim()) {
            case "title" -> "title";
            case "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            case "publishedAt" -> "publishedAt";
            case "viewCount" -> "viewCount";
            default -> defaultSort;
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, property));
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
