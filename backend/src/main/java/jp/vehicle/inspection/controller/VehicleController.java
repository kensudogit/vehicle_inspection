package jp.vehicle.inspection.controller;

import jp.vehicle.inspection.domain.entity.Vehicle;
import jp.vehicle.inspection.dto.VehicleRequest;
import jp.vehicle.inspection.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public List<Vehicle> list() {
        return vehicleService.findAll();
    }

    @GetMapping("/{id}")
    public Vehicle get(@PathVariable Long id) {
        return vehicleService.findById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Vehicle> byCustomer(@PathVariable Long customerId) {
        return vehicleService.findByCustomer(customerId);
    }

    @PostMapping
    public Vehicle create(@Valid @RequestBody VehicleRequest request) {
        return vehicleService.create(request);
    }

    @PutMapping("/{id}")
    public Vehicle update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return vehicleService.update(id, request);
    }

    @PostMapping("/validate")
    public Map<String, Boolean> validate(@RequestBody VehicleRequest request) {
        return Map.of(
                "registrationValid", jp.vehicle.inspection.util.VehicleValidation.isValidRegistrationNumber(request.getRegistrationNumber()),
                "chassisValid", jp.vehicle.inspection.util.VehicleValidation.isValidChassisNumber(request.getChassisNumber()));
    }
}
