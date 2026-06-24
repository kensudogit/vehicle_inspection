package jp.vehicle.inspection.service;

import jp.vehicle.inspection.audit.AuditService;
import jp.vehicle.inspection.domain.entity.MaintenanceRecord;
import jp.vehicle.inspection.domain.repository.MaintenanceRecordRepository;
import jp.vehicle.inspection.dto.MaintenanceRecordRequest;
import jp.vehicle.inspection.exception.BusinessException;
import jp.vehicle.inspection.security.CurrentUserService;
import jp.vehicle.inspection.util.MaintenanceHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRecordRepository repository;
    private final MaintenanceHashUtil hashUtil;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public List<MaintenanceRecord> findByVehicle(Long vehicleId) {
        return repository.findByVehicleIdOrderByPerformedAtDesc(vehicleId);
    }

    public MaintenanceRecord findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Maintenance record not found: " + id));
    }

    @Transactional
    public MaintenanceRecord create(MaintenanceRecordRequest req) {
        Long userId = currentUserService.getCurrentUserId();
        String previousHash = repository.findTopByVehicleIdOrderByCreatedAtDesc(req.getVehicleId())
                .map(MaintenanceRecord::getContentHash)
                .orElse(null);

        MaintenanceRecord record = MaintenanceRecord.builder()
                .vehicleId(req.getVehicleId())
                .performedAt(req.getPerformedAt())
                .workType(req.getWorkType())
                .description(req.getDescription())
                .mileage(req.getMileage())
                .technicianId(req.getTechnicianId())
                .partsUsed(req.getPartsUsed())
                .laborHours(req.getLaborHours())
                .previousHash(previousHash)
                .locked(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .createdBy(userId)
                .build();
        record.setContentHash(hashUtil.computeHash(record));
        MaintenanceRecord saved = repository.save(record);
        auditService.log("CREATE", "MAINTENANCE", saved.getId(), null, saved);
        return saved;
    }

    @Transactional
    public MaintenanceRecord lock(Long id) {
        MaintenanceRecord record = findById(id);
        if (record.isLocked()) {
            throw new BusinessException("記録は既にロックされています（改ざん防止）");
        }
        record.setLocked(true);
        record.setLockedAt(OffsetDateTime.now());
        record.setUpdatedAt(OffsetDateTime.now());
        MaintenanceRecord saved = repository.save(record);
        auditService.log("LOCK", "MAINTENANCE", id, null, saved);
        return saved;
    }

    public boolean verifyIntegrity(Long id) {
        MaintenanceRecord record = findById(id);
        return hashUtil.computeHash(record).equals(record.getContentHash());
    }
}
