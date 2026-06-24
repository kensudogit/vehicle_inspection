package jp.vehicle.inspection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VehicleInspectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleInspectionApplication.class, args);
    }
}
