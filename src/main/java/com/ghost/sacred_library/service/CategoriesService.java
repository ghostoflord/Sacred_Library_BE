package com.ghost.sacred_library.service;

import com.ghost.sacred_library.dto.CategoryRequest;
import com.ghost.sacred_library.dto.CategoryResponse;
import com.ghost.sacred_library.entity.Categories;
import com.ghost.sacred_library.repository.CategoriesRepository;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;

    public CategoriesService(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoriesRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        String slug = resolveSlug(request.slug(), name);
        ensureSlugAvailable(slug, null);

        Categories category = new Categories(
                name,
                normalizeNullable(request.description()),
                slug,
                normalizeNullable(request.categories_image_url()));
        return CategoryResponse.from(categoriesRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Categories category = getCategory(id);
        String name = request.name().trim();
        String slug = resolveSlug(request.slug(), name);
        ensureSlugAvailable(slug, id);

        category.setName(name);
        category.setDescription(normalizeNullable(request.description()));
        category.setSlug(slug);
        category.setCategories_image_url(normalizeNullable(request.categories_image_url()));
        return CategoryResponse.from(categoriesRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoriesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found.");
        }
        categoriesRepository.deleteById(id);
    }

    private Categories getCategory(Long id) {
        return categoriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }

    private void ensureSlugAvailable(String slug, Long currentId) {
        boolean taken = currentId == null
                ? categoriesRepository.existsBySlug(slug)
                : categoriesRepository.existsBySlugAndIdNot(slug, currentId);
        if (taken) {
            throw new DuplicateSlugException("Slug is already in use.");
        }
    }

    private String resolveSlug(String rawSlug, String name) {
        String source = rawSlug == null || rawSlug.isBlank() ? name : rawSlug.trim();
        String slug = toSlug(source);
        if (slug.isBlank()) {
            throw new IllegalArgumentException("Slug is required.");
        }
        return slug;
    }

    private static String toSlug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return normalized;
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
