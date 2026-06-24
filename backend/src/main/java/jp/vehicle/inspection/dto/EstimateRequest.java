package jp.vehicle.inspection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class EstimateRequest {
    @NotNull
    private Long vehicleId;
    @NotNull
    private Long customerId;
    private LocalDate validUntil;
    private String notes;
    private List<EstimateItemRequest> items;
}
