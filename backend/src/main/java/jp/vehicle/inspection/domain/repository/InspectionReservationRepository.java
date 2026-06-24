package jp.vehicle.inspection.domain.repository;

import jp.vehicle.inspection.domain.entity.InspectionReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface InspectionReservationRepository extends JpaRepository<InspectionReservation, Long> {
    List<InspectionReservation> findByReservedAtBetween(OffsetDateTime from, OffsetDateTime to);
    List<InspectionReservation> findByStatus(String status);
}
