package jp.vehicle.inspection.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class OssIntegrationService {

    @Value("${app.integration.oss.enabled:false}")
    private boolean enabled;

    @Value("${app.integration.oss.base-url:}")
    private String baseUrl;

    public void notifyOrderCreated(Long estimateId, Map<String, Object> payload) {
        if (!enabled) {
            log.debug("OSS disabled — skip order notify for estimate {}", estimateId);
            return;
        }
        log.info("OSS notify estimate {} → {}", estimateId, baseUrl);
    }
}
