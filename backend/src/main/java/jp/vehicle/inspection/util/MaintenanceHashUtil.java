package jp.vehicle.inspection.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.vehicle.inspection.domain.entity.MaintenanceRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MaintenanceHashUtil {

    private final ObjectMapper objectMapper;

    public String computeHash(MaintenanceRecord record) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("vehicleId", record.getVehicleId());
            payload.put("performedAt", record.getPerformedAt() != null ? record.getPerformedAt().toString() : null);
            payload.put("workType", record.getWorkType());
            payload.put("description", record.getDescription());
            payload.put("mileage", record.getMileage());
            payload.put("technicianId", record.getTechnicianId());
            payload.put("partsUsed", record.getPartsUsed());
            payload.put("laborHours", record.getLaborHours());
            payload.put("previousHash", record.getPreviousHash());
            String json = objectMapper.writeValueAsString(payload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Hash computation failed", e);
        }
    }
}
