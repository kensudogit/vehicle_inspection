package jp.vehicle.inspection.service;

import jp.vehicle.inspection.domain.entity.Customer;
import jp.vehicle.inspection.domain.entity.Notification;
import jp.vehicle.inspection.domain.entity.Vehicle;
import jp.vehicle.inspection.domain.repository.CustomerRepository;
import jp.vehicle.inspection.domain.repository.NotificationRepository;
import jp.vehicle.inspection.domain.repository.VehicleRepository;
import jp.vehicle.inspection.integration.EmailSender;
import jp.vehicle.inspection.integration.SmsSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;
    private final SmsSender smsSender;

    @Value("${app.notification.expiry-days-before:30,14,7,1}")
    private String expiryDaysBefore;

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Tokyo")
    @Transactional
    public void scheduleExpiryNotifications() {
        List<Integer> daysList = Arrays.stream(expiryDaysBefore.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        LocalDate today = LocalDate.now();
        for (int days : daysList) {
            LocalDate target = today.plusDays(days);
            List<Vehicle> vehicles = vehicleRepository.findExpiringBetween(target, target);
            for (Vehicle vehicle : vehicles) {
                sendExpiryNotification(vehicle, days);
            }
        }
        processPending();
    }

    private void sendExpiryNotification(Vehicle vehicle, int daysBefore) {
        Customer customer = customerRepository.findById(vehicle.getCustomerId()).orElse(null);
        if (customer == null) return;

        String subject = String.format("【車検満了のお知らせ】あと%d日 — %s", daysBefore, vehicle.getRegistrationNumber());
        String body = String.format(
                "%s 様\n\nお車（%s / 車台番号 %s）の車検満了日は %s です。\nご予約はお早めにお願いいたします。",
                customer.getName(),
                vehicle.getRegistrationNumber(),
                vehicle.getChassisNumber(),
                vehicle.getInspectionExpiry().format(DateTimeFormatter.ISO_LOCAL_DATE));

        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            queue("EMAIL", "INSPECTION_EXPIRY", customer.getEmail(), subject, body, customer.getId(), vehicle.getId());
        }
        if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
            queue("SMS", "INSPECTION_EXPIRY", customer.getPhone(), subject, body, customer.getId(), vehicle.getId());
        }
    }

    private void queue(String channel, String type, String recipient, String subject, String body,
                       Long customerId, Long vehicleId) {
        Notification n = Notification.builder()
                .channel(channel)
                .notificationType(type)
                .recipient(recipient)
                .subject(subject)
                .body(body)
                .customerId(customerId)
                .vehicleId(vehicleId)
                .status("PENDING")
                .createdAt(OffsetDateTime.now())
                .build();
        notificationRepository.save(n);
    }

    @Transactional
    public void processPending() {
        for (Notification n : notificationRepository.findByStatus("PENDING")) {
            try {
                if ("EMAIL".equals(n.getChannel())) {
                    emailSender.send(n.getRecipient(), n.getSubject(), n.getBody());
                } else if ("SMS".equals(n.getChannel())) {
                    smsSender.send(n.getRecipient(), n.getBody());
                }
                n.setStatus("SENT");
                n.setSentAt(OffsetDateTime.now());
            } catch (Exception e) {
                n.setStatus("FAILED");
                n.setErrorMessage(e.getMessage());
                log.error("Notification failed: {}", e.getMessage());
            }
            notificationRepository.save(n);
        }
    }
}
