package jp.vehicle.inspection.domain.repository;

import jp.vehicle.inspection.domain.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByVehicleIdOrderByPerformedAtDesc(Long vehicleId);
    Optional<MaintenanceRecord> findTopByVehicleIdOrderByCreatedAtDesc(Long vehicleId);
}
