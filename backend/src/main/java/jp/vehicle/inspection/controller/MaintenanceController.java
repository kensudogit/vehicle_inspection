package jp.vehicle.inspection.controller;

import jp.vehicle.inspection.domain.entity.MaintenanceRecord;
import jp.vehicle.inspection.dto.MaintenanceRecordRequest;
import jp.vehicle.inspection.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping("/vehicle/{vehicleId}")
    public List<MaintenanceRecord> byVehicle(@PathVariable Long vehicleId) {
        return maintenanceService.findByVehicle(vehicleId);
    }

    @PostMapping
    public MaintenanceRecord create(@Valid @RequestBody MaintenanceRecordRequest request) {
        return maintenanceService.create(request);
    }

    @PostMapping("/{id}/lock")
    public MaintenanceRecord lock(@PathVariable Long id) {
        return maintenanceService.lock(id);
    }

    @GetMapping("/{id}/verify")
    public Map<String, Boolean> verify(@PathVariable Long id) {
        return Map.of("valid", maintenanceService.verifyIntegrity(id));
    }
}
