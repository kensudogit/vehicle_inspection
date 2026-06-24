package jp.vehicle.inspection.integration;

public interface SmsSender {
    void send(String phone, String message);
}
