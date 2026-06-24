package jp.vehicle.inspection.domain.repository;

import jp.vehicle.inspection.domain.entity.VehicleInspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleInspectionRepository extends JpaRepository<VehicleInspection, Long> {
    List<VehicleInspection> findByVehicleIdOrderByInspectionDateDesc(Long vehicleId);
}
