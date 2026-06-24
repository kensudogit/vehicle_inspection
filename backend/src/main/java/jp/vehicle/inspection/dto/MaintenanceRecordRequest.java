package jp.vehicle.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class MaintenanceRecordRequest {
    @NotNull
    private Long vehicleId;
    @NotNull
    private OffsetDateTime performedAt;
    @NotBlank
    private String workType;
    @NotBlank
    private String description;
    private Integer mileage;
    private Long technicianId;
    private List<Map<String, Object>> partsUsed;
    private BigDecimal laborHours;
}
