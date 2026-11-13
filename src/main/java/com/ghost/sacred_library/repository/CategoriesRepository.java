package com.ghost.sacred_library.repository;

import com.ghost.sacred_library.entity.Categories;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    Optional<Categories> findBySlug(String slug);
}
