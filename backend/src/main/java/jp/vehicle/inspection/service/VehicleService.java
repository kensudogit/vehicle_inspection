package jp.vehicle.inspection.service;

import jp.vehicle.inspection.audit.AuditService;
import jp.vehicle.inspection.domain.entity.Vehicle;
import jp.vehicle.inspection.domain.repository.VehicleRepository;
import jp.vehicle.inspection.dto.VehicleRequest;
import jp.vehicle.inspection.exception.BusinessException;
import jp.vehicle.inspection.security.CurrentUserService;
import jp.vehicle.inspection.util.VehicleValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Vehicle not found: " + id));
    }

    public List<Vehicle> findByCustomer(Long customerId) {
        return vehicleRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Vehicle create(VehicleRequest req) {
        validateNumbers(req.getRegistrationNumber(), req.getChassisNumber(), null, null);
        Long userId = currentUserService.getCurrentUserId();
        Vehicle vehicle = Vehicle.builder()
                .customerId(req.getCustomerId())
                .registrationNumber(VehicleValidation.normalizeRegistrationNumber(req.getRegistrationNumber()))
                .chassisNumber(VehicleValidation.normalizeChassisNumber(req.getChassisNumber()))
                .maker(req.getMaker())
                .model(req.getModel())
                .modelYear(req.getModelYear())
                .engineDisplacement(req.getEngineDisplacement())
                .fuelType(req.getFuelType())
                .color(req.getColor())
                .firstRegistration(req.getFirstRegistration())
                .inspectionExpiry(req.getInspectionExpiry())
                .mileage(req.getMileage() != null ? req.getMileage() : 0)
                .build();
        vehicle.setCreatedBy(userId);
        vehicle.setUpdatedBy(userId);
        Vehicle saved = vehicleRepository.save(vehicle);
        auditService.log("CREATE", "VEHICLE", saved.getId(), null, saved);
        return saved;
    }

    @Transactional
    public Vehicle update(Long id, VehicleRequest req) {
        Vehicle vehicle = findById(id);
        validateNumbers(req.getRegistrationNumber(), req.getChassisNumber(), id, vehicle.getChassisNumber());
        vehicle.setRegistrationNumber(VehicleValidation.normalizeRegistrationNumber(req.getRegistrationNumber()));
        vehicle.setChassisNumber(VehicleValidation.normalizeChassisNumber(req.getChassisNumber()));
        vehicle.setMaker(req.getMaker());
        vehicle.setModel(req.getModel());
        vehicle.setModelYear(req.getModelYear());
        vehicle.setEngineDisplacement(req.getEngineDisplacement());
        vehicle.setFuelType(req.getFuelType());
        vehicle.setColor(req.getColor());
        vehicle.setFirstRegistration(req.getFirstRegistration());
        vehicle.setInspectionExpiry(req.getInspectionExpiry());
        if (req.getMileage() != null) vehicle.setMileage(req.getMileage());
        vehicle.setUpdatedBy(currentUserService.getCurrentUserId());
        Vehicle saved = vehicleRepository.save(vehicle);
        auditService.log("UPDATE", "VEHICLE", id, null, saved);
        return saved;
    }

    private void validateNumbers(String reg, String chassis, Long excludeId, String currentChassis) {
        if (!VehicleValidation.isValidRegistrationNumber(reg)) {
            throw new BusinessException("登録番号の形式が正しくありません（例: 品川500あ1234）");
        }
        if (!VehicleValidation.isValidChassisNumber(chassis)) {
            throw new BusinessException("車台番号は17桁の英数字（I/O/Q除く）で入力してください");
        }
        String normReg = VehicleValidation.normalizeRegistrationNumber(reg);
        String normChassis = VehicleValidation.normalizeChassisNumber(chassis);
        vehicleRepository.findByRegistrationNumber(normReg).ifPresent(v -> {
            if (excludeId == null || !v.getId().equals(excludeId)) {
                throw new BusinessException("登録番号は既に登録されています: " + normReg);
            }
        });
        vehicleRepository.findByChassisNumber(normChassis).ifPresent(v -> {
            if (excludeId == null || !v.getId().equals(excludeId)) {
                throw new BusinessException("車台番号は既に登録されています: " + normChassis);
            }
        });
    }
}
