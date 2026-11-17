package com.ghost.sacred_library.repository;

import com.ghost.sacred_library.entity.Article;
import com.ghost.sacred_library.enums.ArticleStatus;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, Long id);

    Optional<Article> findByIdAndDeletedAtIsNull(Long id);

    Optional<Article> findBySlugAndDeletedAtIsNull(String slug);

    @Query("""
            SELECT a FROM Article a
            WHERE a.deletedAt IS NULL
              AND (:status IS NULL OR a.status = :status)
              AND (:categoryId IS NULL OR a.category.id = :categoryId)
              AND (:authorId IS NULL OR a.author.id = :authorId)
              AND (
                    :q IS NULL
                    OR LOWER(a.title) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(a.summary, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            """)
    Page<Article> searchAdmin(
            @Param("q") String q,
            @Param("status") ArticleStatus status,
            @Param("categoryId") Long categoryId,
            @Param("authorId") Long authorId,
            Pageable pageable);

    @Query("""
            SELECT a FROM Article a
            WHERE a.deletedAt IS NULL
              AND a.status = com.ghost.sacred_library.enums.ArticleStatus.PUBLISHED
              AND a.publishedAt IS NOT NULL
              AND a.publishedAt <= :now
              AND (:categoryId IS NULL OR a.category.id = :categoryId)
              AND (
                    :q IS NULL
                    OR LOWER(a.title) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(a.summary, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            """)
    Page<Article> searchPublished(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("now") Instant now,
            Pageable pageable);

    @Query("""
            SELECT a FROM Article a
            WHERE a.deletedAt IS NULL
              AND a.status = com.ghost.sacred_library.enums.ArticleStatus.PUBLISHED
              AND a.featured = true
              AND a.publishedAt IS NOT NULL
              AND a.publishedAt <= :now
            ORDER BY a.publishedAt DESC
            """)
    Page<Article> findFeaturedPublished(@Param("now") Instant now, Pageable pageable);
}
