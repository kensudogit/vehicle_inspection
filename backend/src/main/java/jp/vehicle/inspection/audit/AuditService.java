package jp.vehicle.inspection.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jp.vehicle.inspection.domain.entity.AuditLog;
import jp.vehicle.inspection.domain.repository.AuditLogRepository;
import jp.vehicle.inspection.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    public void log(String action, String entityType, Long entityId, Object oldValue, Object newValue) {
        AuditLog log = AuditLog.builder()
                .userId(currentUserService.getCurrentUserId())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(toMap(oldValue))
                .newValue(toMap(newValue))
                .ipAddress(resolveIp())
                .userAgent(resolveUserAgent())
                .createdAt(OffsetDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        if (value == null) return null;
        if (value instanceof Map) return (Map<String, Object>) value;
        return objectMapper.convertValue(value, Map.class);
    }

    private String resolveIp() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest req = attrs.getRequest();
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private String resolveUserAgent() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        return attrs.getRequest().getHeader("User-Agent");
    }
}
