package jp.vehicle.inspection.service;

import jp.vehicle.inspection.audit.AuditService;
import jp.vehicle.inspection.domain.entity.Vehicle;
import jp.vehicle.inspection.domain.entity.VehicleInspection;
import jp.vehicle.inspection.domain.repository.VehicleInspectionRepository;
import jp.vehicle.inspection.domain.repository.VehicleRepository;
import jp.vehicle.inspection.exception.BusinessException;
import jp.vehicle.inspection.integration.ElectronicInspectionClient;
import jp.vehicle.inspection.security.CurrentUserService;
import jp.vehicle.inspection.util.VehicleValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElectronicInspectionService {

    private final ElectronicInspectionClient client;
    private final VehicleRepository vehicleRepository;
    private final VehicleInspectionRepository inspectionRepository;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    @Transactional
    public VehicleInspection importByCertId(Long vehicleId, String certId) {
        Map<String, Object> cert = client.fetchCertificate(certId);
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException("Vehicle not found"));

        String reg = String.valueOf(cert.get("registrationNumber"));
        String chassis = String.valueOf(cert.get("chassisNumber"));
        if (!VehicleValidation.normalizeRegistrationNumber(reg)
                .equals(vehicle.getRegistrationNumber())) {
            throw new BusinessException("登録番号が車両データと一致しません");
        }
        if (!VehicleValidation.normalizeChassisNumber(chassis)
                .equals(vehicle.getChassisNumber())) {
            throw new BusinessException("車台番号が車両データと一致しません");
        }

        LocalDate expiry = LocalDate.parse(String.valueOf(cert.get("expiryDate")));
        vehicle.setInspectionExpiry(expiry);
        vehicleRepository.save(vehicle);

        VehicleInspection inspection = VehicleInspection.builder()
                .vehicleId(vehicleId)
                .inspectionType("ELECTRONIC")
                .inspectionDate(LocalDate.parse(String.valueOf(cert.get("inspectionDate"))))
                .expiryDate(expiry)
                .result(String.valueOf(cert.get("result")))
                .electronicCertId(certId)
                .electronicData(client.importFromPayload(cert))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .createdBy(currentUserService.getCurrentUserId())
                .build();
        VehicleInspection saved = inspectionRepository.save(inspection);
        auditService.log("IMPORT_ELECTRONIC", "VEHICLE_INSPECTION", saved.getId(), null, saved);
        return saved;
    }

    public List<VehicleInspection> findByVehicle(Long vehicleId) {
        return inspectionRepository.findByVehicleIdOrderByInspectionDateDesc(vehicleId);
    }
}
