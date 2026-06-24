package jp.vehicle.inspection.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private List<String> roles;
    private boolean mfaRequired;
}
