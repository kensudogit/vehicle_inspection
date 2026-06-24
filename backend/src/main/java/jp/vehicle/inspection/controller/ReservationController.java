package jp.vehicle.inspection.controller;

import jp.vehicle.inspection.domain.entity.InspectionReservation;
import jp.vehicle.inspection.dto.ReservationRequest;
import jp.vehicle.inspection.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public List<InspectionReservation> list() {
        return reservationService.findAll();
    }

    @GetMapping("/range")
    public List<InspectionReservation> byRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return reservationService.findByDateRange(from, to);
    }

    @PostMapping
    public InspectionReservation create(@Valid @RequestBody ReservationRequest request) {
        return reservationService.create(request);
    }

    @PatchMapping("/{id}/status")
    public InspectionReservation updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return reservationService.updateStatus(id, body.get("status"));
    }
}
