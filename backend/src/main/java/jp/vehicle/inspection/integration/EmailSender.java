package jp.vehicle.inspection.integration;

public interface EmailSender {
    void send(String to, String subject, String body);
}
