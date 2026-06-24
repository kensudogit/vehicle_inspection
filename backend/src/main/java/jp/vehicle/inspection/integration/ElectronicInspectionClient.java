package jp.vehicle.inspection.integration;

import java.util.Map;

public interface ElectronicInspectionClient {
    Map<String, Object> fetchCertificate(String certId);
    Map<String, Object> importFromPayload(Map<String, Object> payload);
    boolean isEnabled();
}
