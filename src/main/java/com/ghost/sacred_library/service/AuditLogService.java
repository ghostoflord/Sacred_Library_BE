package com.ghost.sacred_library.service;

import com.ghost.sacred_library.dto.AuditLogResponse;
import com.ghost.sacred_library.dto.PageResponse;
import com.ghost.sacred_library.entity.AuditLog;
import com.ghost.sacred_library.repository.AuditLogRepository;
import com.ghost.sacred_library.security.UserPrincipal;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(String action, String entityType, Long entityId, Map<String, ?> metadata) {
        Long actorId = currentUserId();
        auditLogRepository.save(new AuditLog(actorId, action, entityType, entityId, toJson(metadata)));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findAll(
            String entityType,
            Long actorUserId,
            String action,
            int page,
            int pageSize) {
        int safePage = Math.max(page, 1) - 1;
        int safeSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(
                auditLogRepository
                        .search(blankToNull(entityType), actorUserId, blankToNull(action), pageable)
                        .map(AuditLogResponse::from));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }

    private static String toJson(Map<String, ?> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        return metadata.entrySet().stream()
                .map(entry -> "\"" + escape(entry.getKey()) + "\":" + valueJson(entry.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    private static String valueJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
