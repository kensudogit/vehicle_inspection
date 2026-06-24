package jp.vehicle.inspection.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ElectronicInspectionClientImpl implements ElectronicInspectionClient {

    @Value("${app.integration.electronic-inspection.enabled:false}")
    private boolean enabled;

    @Value("${app.integration.electronic-inspection.base-url:}")
    private String baseUrl;

    @Override
    public Map<String, Object> fetchCertificate(String certId) {
        if (!enabled) {
            return mockCertificate(certId);
        }
        log.info("Fetching electronic certificate {} from {}", certId, baseUrl);
        // Production: REST call to government electronic inspection API
        return mockCertificate(certId);
    }

    @Override
    public Map<String, Object> importFromPayload(Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>(payload);
        result.put("imported", true);
        result.put("source", enabled ? "ELECTRONIC_API" : "MANUAL_IMPORT");
        return result;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private Map<String, Object> mockCertificate(String certId) {
        Map<String, Object> cert = new HashMap<>();
        cert.put("certId", certId);
        cert.put("registrationNumber", "品川500あ1234");
        cert.put("chassisNumber", "JTDBT9235030123456");
        cert.put("inspectionDate", "2025-06-01");
        cert.put("expiryDate", "2027-05-31");
        cert.put("result", "PASS");
        return cert;
    }
}
