package jp.vehicle.inspection.service;

import jp.vehicle.inspection.audit.AuditService;
import jp.vehicle.inspection.domain.entity.InspectionReservation;
import jp.vehicle.inspection.domain.repository.InspectionReservationRepository;
import jp.vehicle.inspection.dto.ReservationRequest;
import jp.vehicle.inspection.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final InspectionReservationRepository repository;
    private final AuditService auditService;

    public List<InspectionReservation> findAll() {
        return repository.findAll();
    }

    public InspectionReservation findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Reservation not found: " + id));
    }

    public List<InspectionReservation> findByDateRange(OffsetDateTime from, OffsetDateTime to) {
        return repository.findByReservedAtBetween(from, to);
    }

    @Transactional
    public InspectionReservation create(ReservationRequest req) {
        InspectionReservation r = InspectionReservation.builder()
                .vehicleId(req.getVehicleId())
                .customerId(req.getCustomerId())
                .reservedAt(req.getReservedAt())
                .serviceType(req.getServiceType() != null ? req.getServiceType() : "INSPECTION")
                .assignedUserId(req.getAssignedUserId())
                .notes(req.getNotes())
                .status("SCHEDULED")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        InspectionReservation saved = repository.save(r);
        auditService.log("CREATE", "RESERVATION", saved.getId(), null, saved);
        return saved;
    }

    @Transactional
    public InspectionReservation updateStatus(Long id, String status) {
        InspectionReservation r = findById(id);
        r.setStatus(status);
        r.setUpdatedAt(OffsetDateTime.now());
        InspectionReservation saved = repository.save(r);
        auditService.log("UPDATE_STATUS", "RESERVATION", id, null, java.util.Map.of("status", status));
        return saved;
    }
}
