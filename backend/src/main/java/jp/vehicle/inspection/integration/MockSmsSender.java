package jp.vehicle.inspection.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockSmsSender implements SmsSender {
    @Override
    public void send(String phone, String message) {
        log.info("SMS mock → {}: {}", phone, message);
    }
}
