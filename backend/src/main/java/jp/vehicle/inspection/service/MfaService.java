package jp.vehicle.inspection.service;

import jp.vehicle.inspection.domain.entity.User;
import jp.vehicle.inspection.domain.repository.UserRepository;
import jp.vehicle.inspection.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final UserRepository userRepository;

    @Value("${app.mfa.issuer:VehicleInspection}")
    private String issuer;

    public String setupMfa(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found"));
        String secret = generateSecret();
        user.setMfaSecret(secret);
        user.setMfaEnabled(false);
        userRepository.save(user);
        return buildOtpAuthUrl(user.getEmail(), secret);
    }

    public void enableMfa(Long userId, String code) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found"));
        if (!verifyCode(user.getMfaSecret(), code)) {
            throw new BusinessException("Invalid MFA code");
        }
        user.setMfaEnabled(true);
        userRepository.save(user);
    }

    public boolean verify(Long userId, String code) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found"));
        if (!user.isMfaEnabled()) return true;
        return verifyCode(user.getMfaSecret(), code);
    }

    private String generateSecret() {
        byte[] buffer = new byte[20];
        new SecureRandom().nextBytes(buffer);
        return new Base32().encodeToString(buffer).replace("=", "");
    }

    private String buildOtpAuthUrl(String email, String secret) {
        String label = URLEncoder.encode(issuer + ":" + email, StandardCharsets.UTF_8);
        return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + URLEncoder.encode(issuer, StandardCharsets.UTF_8);
    }

    private boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) return false;
        long time = System.currentTimeMillis() / 1000 / 30;
        for (int i = -1; i <= 1; i++) {
            if (code.equals(generateTotp(secret, time + i))) return true;
        }
        return false;
    }

    private String generateTotp(String secret, long counter) {
        try {
            Base32 base32 = new Base32();
            byte[] key = base32.decode(secret.toUpperCase());
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);
            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (Exception e) {
            return "";
        }
    }
}
