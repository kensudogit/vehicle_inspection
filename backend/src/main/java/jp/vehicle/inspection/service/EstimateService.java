package jp.vehicle.inspection.service;

import jp.vehicle.inspection.audit.AuditService;
import jp.vehicle.inspection.domain.entity.Estimate;
import jp.vehicle.inspection.domain.entity.EstimateItem;
import jp.vehicle.inspection.domain.repository.EstimateRepository;
import jp.vehicle.inspection.dto.EstimateItemRequest;
import jp.vehicle.inspection.dto.EstimateRequest;
import jp.vehicle.inspection.exception.BusinessException;
import jp.vehicle.inspection.integration.OssIntegrationService;
import jp.vehicle.inspection.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class EstimateService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final EstimateRepository estimateRepository;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;
    private final OssIntegrationService ossIntegrationService;

    public List<Estimate> findAll() {
        return estimateRepository.findAll();
    }

    public Estimate findById(Long id) {
        return estimateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estimate not found: " + id));
    }

    @Transactional
    public Estimate create(EstimateRequest req) {
        Long userId = currentUserService.getCurrentUserId();
        Estimate estimate = Estimate.builder()
                .estimateNumber("EST-" + SEQ.incrementAndGet())
                .vehicleId(req.getVehicleId())
                .customerId(req.getCustomerId())
                .status("DRAFT")
                .validUntil(req.getValidUntil())
                .notes(req.getNotes())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .createdBy(userId)
                .build();

        if (req.getItems() != null) {
            for (EstimateItemRequest itemReq : req.getItems()) {
                BigDecimal qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : BigDecimal.ONE;
                BigDecimal price = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal amount = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
                EstimateItem item = EstimateItem.builder()
                        .estimate(estimate)
                        .itemType(itemReq.getItemType() != null ? itemReq.getItemType() : "LABOR")
                        .description(itemReq.getDescription())
                        .quantity(qty)
                        .unitPrice(price)
                        .amount(amount)
                        .partId(itemReq.getPartId())
                        .build();
                estimate.getItems().add(item);
            }
        }
        recalculate(estimate);
        Estimate saved = estimateRepository.save(estimate);
        ossIntegrationService.notifyOrderCreated(saved.getId(), Map.of("estimateNumber", saved.getEstimateNumber()));
        auditService.log("CREATE", "ESTIMATE", saved.getId(), null, saved);
        return saved;
    }

    private void recalculate(Estimate estimate) {
        BigDecimal subtotal = estimate.getItems().stream()
                .map(EstimateItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        estimate.setSubtotal(subtotal);
        estimate.setTaxAmount(tax);
        estimate.setTotalAmount(subtotal.add(tax));
    }
}
