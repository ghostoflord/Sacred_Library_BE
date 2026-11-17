package com.ghost.sacred_library.repository;

import com.ghost.sacred_library.entity.Tag;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Tag> findBySlug(String slug);

    List<Tag> findByIdIn(Collection<Long> ids);

    @Query("""
            SELECT DISTINCT t FROM Article a
            JOIN a.tags t
            WHERE a.deletedAt IS NULL
              AND a.status = com.ghost.sacred_library.enums.ArticleStatus.PUBLISHED
              AND a.publishedAt IS NOT NULL
              AND a.publishedAt <= CURRENT_TIMESTAMP
            ORDER BY t.name ASC
            """)
    List<Tag> findUsedByPublishedArticles();

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "DELETE FROM article_tags WHERE tag_id = :tagId", nativeQuery = true)
    void deleteArticleLinks(@Param("tagId") Long tagId);
}
