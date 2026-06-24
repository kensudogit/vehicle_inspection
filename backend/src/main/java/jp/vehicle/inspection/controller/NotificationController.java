package jp.vehicle.inspection.controller;

import jp.vehicle.inspection.domain.entity.Notification;
import jp.vehicle.inspection.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<Notification> list() {
        return notificationService.findAll();
    }

    @PostMapping("/process")
    public Map<String, String> processPending() {
        notificationService.processPending();
        return Map.of("status", "processed");
    }

    @PostMapping("/check-expiry")
    public Map<String, String> checkExpiry() {
        notificationService.scheduleExpiryNotifications();
        return Map.of("status", "scheduled");
    }
}
