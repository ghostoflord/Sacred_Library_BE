package com.ghost.sacred_library.repository;

import com.ghost.sacred_library.entity.ArticleSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleSourceRepository extends JpaRepository<ArticleSource, Long> {
    List<ArticleSource> findByArticleIdOrderByIdAsc(Long articleId);

    Optional<ArticleSource> findByIdAndArticleDeletedAtIsNull(Long id);
}
