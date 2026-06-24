package jp.vehicle.inspection.controller;

import jp.vehicle.inspection.domain.entity.AuditLog;
import jp.vehicle.inspection.domain.repository.AuditLogRepository;
import jp.vehicle.inspection.security.CurrentUserService;
import jp.vehicle.inspection.service.MfaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;
    private final CurrentUserService currentUserService;
    private final AuditLogRepository auditLogRepository;

    @PostMapping("/mfa/setup")
    public Map<String, String> setup() {
        Long userId = currentUserService.getCurrentUserId();
        String url = mfaService.setupMfa(userId);
        return Map.of("otpAuthUrl", url);
    }

    @PostMapping("/mfa/enable")
    public Map<String, String> enable(@RequestBody Map<String, String> body) {
        mfaService.enableMfa(currentUserService.getCurrentUserId(), body.get("code"));
        return Map.of("status", "enabled");
    }

    @GetMapping("/audit-logs")
    public List<AuditLog> auditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        if (entityType != null && entityId != null) {
            return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
        }
        return auditLogRepository.findAll();
    }
}
