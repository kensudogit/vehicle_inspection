package jp.vehicle.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ReservationRequest {
    @NotNull
    private Long vehicleId;
    @NotNull
    private Long customerId;
    @NotNull
    private OffsetDateTime reservedAt;
    private String serviceType;
    private Long assignedUserId;
    private String notes;
}
