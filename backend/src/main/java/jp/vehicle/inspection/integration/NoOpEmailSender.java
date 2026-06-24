package jp.vehicle.inspection.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.integration.email.enabled", havingValue = "false")
public class NoOpEmailSender implements EmailSender {
    @Override
    public void send(String to, String subject, String body) {
        log.warn("Email disabled — would send to {}: {}", to, subject);
    }
}
