package com.ghost.sacred_library.dto;

import com.ghost.sacred_library.entity.AuditLog;
import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String action,
        String entityType,
        Long entityId,
        String metadataJson,
        Instant createdAt) {

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorUserId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getMetadataJson(),
                log.getCreatedAt());
    }
}
