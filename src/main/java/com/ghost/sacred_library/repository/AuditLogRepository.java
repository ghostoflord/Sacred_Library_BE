package com.ghost.sacred_library.repository;

import com.ghost.sacred_library.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:entityType IS NULL OR a.entityType = :entityType)
              AND (:actorUserId IS NULL OR a.actorUserId = :actorUserId)
              AND (:action IS NULL OR a.action = :action)
            """)
    Page<AuditLog> search(
            @Param("entityType") String entityType,
            @Param("actorUserId") Long actorUserId,
            @Param("action") String action,
            Pageable pageable);
}
