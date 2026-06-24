package jp.vehicle.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotBlank
    private String name;
    private String nameKana;
    private String email;
    private String phone;
    private String postalCode;
    private String address;
    private String notes;
    private boolean consentMarketing;
}
