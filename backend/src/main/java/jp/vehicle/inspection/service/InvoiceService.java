package jp.vehicle.inspection.service;

import jp.vehicle.inspection.audit.AuditService;
import jp.vehicle.inspection.domain.entity.Estimate;
import jp.vehicle.inspection.domain.entity.Invoice;
import jp.vehicle.inspection.domain.entity.Payment;
import jp.vehicle.inspection.domain.repository.InvoiceRepository;
import jp.vehicle.inspection.domain.repository.PaymentRepository;
import jp.vehicle.inspection.exception.BusinessException;
import jp.vehicle.inspection.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final EstimateService estimateService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Invoice not found: " + id));
    }

    @Transactional
    public Invoice createFromEstimate(Long estimateId) {
        Estimate estimate = estimateService.findById(estimateId);
        Long userId = currentUserService.getCurrentUserId();
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + SEQ.incrementAndGet())
                .estimateId(estimateId)
                .vehicleId(estimate.getVehicleId())
                .customerId(estimate.getCustomerId())
                .status("UNPAID")
                .subtotal(estimate.getSubtotal())
                .taxAmount(estimate.getTaxAmount())
                .totalAmount(estimate.getTotalAmount())
                .issuedAt(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .createdBy(userId)
                .build();
        Invoice saved = invoiceRepository.save(invoice);
        auditService.log("CREATE", "INVOICE", saved.getId(), null, saved);
        return saved;
    }

    @Transactional
    public Payment recordPayment(Long invoiceId, BigDecimal amount, String method, String reference) {
        Invoice invoice = findById(invoiceId);
        Payment payment = Payment.builder()
                .invoiceId(invoiceId)
                .amount(amount)
                .paymentMethod(method != null ? method : "CASH")
                .paidAt(OffsetDateTime.now())
                .reference(reference)
                .createdBy(currentUserService.getCurrentUserId())
                .build();
        Payment saved = paymentRepository.save(payment);

        BigDecimal paid = paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (paid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus("PAID");
        } else {
            invoice.setStatus("PARTIAL");
        }
        invoice.setUpdatedAt(OffsetDateTime.now());
        invoiceRepository.save(invoice);
        auditService.log("PAYMENT", "INVOICE", invoiceId, null, saved);
        return saved;
    }
}
