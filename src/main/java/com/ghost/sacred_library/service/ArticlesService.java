package com.ghost.sacred_library.service;

import com.ghost.sacred_library.dto.ArticleDetailResponse;
import com.ghost.sacred_library.dto.ArticleListItemResponse;
import com.ghost.sacred_library.dto.ArticleRequest;
import com.ghost.sacred_library.dto.PageResponse;
import com.ghost.sacred_library.entity.Article;
import com.ghost.sacred_library.entity.Categories;
import com.ghost.sacred_library.entity.User;
import com.ghost.sacred_library.enums.ArticleStatus;
import com.ghost.sacred_library.enums.ContentFormat;
import com.ghost.sacred_library.repository.ArticleRepository;
import com.ghost.sacred_library.repository.CategoriesRepository;
import com.ghost.sacred_library.repository.UserRepository;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
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

    public ArticlesService(
            ArticleRepository articleRepository,
            CategoriesRepository categoriesRepository,
            UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.categoriesRepository = categoriesRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ArticleDetailResponse create(ArticleRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found."));
        Categories category = getCategory(request.categoryId());
        String title = request.title().trim();
        String content = request.content().trim();
        String slug = resolveSlug(request.slug(), title);
        ensureSlugAvailable(slug, null);

        Article article = new Article(title, slug, content, category, author);
        applyWritableFields(article, request, title, content);
        article.setStatus(ArticleStatus.DRAFT);
        article.setPublishedAt(null);
        return ArticleDetailResponse.from(articleRepository.save(article));
    }

    @Transactional
    public ArticleDetailResponse update(Long id, ArticleRequest request) {
        Article article = getActiveArticle(id);
        Categories category = getCategory(request.categoryId());
        String title = request.title().trim();
        String content = request.content().trim();
        String slug = resolveSlug(request.slug(), title);
        ensureSlugAvailable(slug, id);

        article.setTitle(title);
        article.setSlug(slug);
        article.setContent(content);
        article.setCategory(category);
        applyWritableFields(article, request, title, content);

        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            validatePublishable(article);
            if (article.getPublishedAt() == null) {
                article.setPublishedAt(Instant.now());
            }
        }
        return ArticleDetailResponse.from(articleRepository.save(article));
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
        return ArticleDetailResponse.from(articleRepository.save(article));
    }

    @Transactional
    public ArticleDetailResponse unpublish(Long id) {
        Article article = getActiveArticle(id);
        article.setStatus(ArticleStatus.DRAFT);
        return ArticleDetailResponse.from(articleRepository.save(article));
    }

    @Transactional
    public ArticleDetailResponse archive(Long id) {
        Article article = getActiveArticle(id);
        article.setStatus(ArticleStatus.ARCHIVED);
        return ArticleDetailResponse.from(articleRepository.save(article));
    }

    @Transactional
    public void softDelete(Long id) {
        Article article = getActiveArticle(id);
        article.softDelete();
        articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleListItemResponse> findPublished(
            String q,
            Long categoryId,
            int page,
            int pageSize,
            String sortBy,
            String sortDirection) {
        Pageable pageable = toPageable(page, pageSize, sortBy, sortDirection, "publishedAt");
        Page<ArticleListItemResponse> result = articleRepository
                .searchPublished(normalizeQuery(q), categoryId, Instant.now(), pageable)
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
                .searchPublished(null, null, Instant.now(), pageable)
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

    private void applyWritableFields(Article article, ArticleRequest request, String title, String content) {
        article.setSummary(normalizeNullable(request.summary()));
        article.setContentFormat(request.contentFormat() == null ? ContentFormat.MARKDOWN : request.contentFormat());
        article.setCoverImageUrl(normalizeNullable(request.coverImageUrl()));
        article.setFeatured(Boolean.TRUE.equals(request.featured()));
        article.setMetaTitle(normalizeNullable(request.metaTitle()));
        article.setMetaDescription(normalizeNullable(request.metaDescription()));
        article.setReadingTimeMinutes(estimateReadingTime(content));
        if (article.getMetaTitle() == null) {
            article.setMetaTitle(title);
        }
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

    private String resolveSlug(String rawSlug, String title) {
        String source = rawSlug == null || rawSlug.isBlank() ? title : rawSlug.trim();
        String slug = toSlug(source);
        if (slug.isBlank()) {
            throw new IllegalArgumentException("Slug is required.");
        }
        return slug;
    }

    private static String toSlug(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
