package jp.vehicle.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleRequest {
    @NotNull
    private Long customerId;
    @NotBlank
    private String registrationNumber;
    @NotBlank
    private String chassisNumber;
    private String maker;
    private String model;
    private Integer modelYear;
    private Integer engineDisplacement;
    private String fuelType;
    private String color;
    private LocalDate firstRegistration;
    @NotNull
    private LocalDate inspectionExpiry;
    private Integer mileage;
}
