package com.ghost.sacred_library.controller;

import com.ghost.sacred_library.dto.AuditLogResponse;
import com.ghost.sacred_library.dto.PageResponse;
import com.ghost.sacred_library.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogsController {

    private final AuditLogService auditLogService;

    public AdminAuditLogsController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public PageResponse<AuditLogResponse> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return auditLogService.findAll(entityType, actorUserId, action, page, pageSize);
    }
}
