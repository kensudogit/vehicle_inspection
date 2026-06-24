package jp.vehicle.inspection.service;

import jp.vehicle.inspection.audit.AuditService;
import jp.vehicle.inspection.domain.entity.User;
import jp.vehicle.inspection.domain.repository.UserRepository;
import jp.vehicle.inspection.dto.AuthResponse;
import jp.vehicle.inspection.dto.LoginRequest;
import jp.vehicle.inspection.exception.BusinessException;
import jp.vehicle.inspection.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final MfaService mfaService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));
        if (user.isMfaEnabled()) {
            if (request.getMfaCode() == null || request.getMfaCode().isBlank()) {
                return AuthResponse.builder().mfaRequired(true).email(user.getEmail()).build();
            }
            if (!mfaService.verify(user.getId(), request.getMfaCode())) {
                throw new BusinessException("MFA verification failed");
            }
        }
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail(), user.getId(), Map.of());
        auditService.log("LOGIN", "USER", user.getId(), null, Map.of("email", user.getEmail()));
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream().map(r -> r.getName()).toList())
                .mfaRequired(false)
                .build();
    }
}
