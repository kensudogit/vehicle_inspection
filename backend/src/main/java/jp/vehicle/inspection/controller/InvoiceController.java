package jp.vehicle.inspection.controller;

import jp.vehicle.inspection.domain.entity.Invoice;
import jp.vehicle.inspection.domain.entity.Payment;
import jp.vehicle.inspection.service.InvoiceService;
import jp.vehicle.inspection.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ReportService reportService;

    @GetMapping
    public List<Invoice> list() {
        return invoiceService.findAll();
    }

    @GetMapping("/{id}")
    public Invoice get(@PathVariable Long id) {
        return invoiceService.findById(id);
    }

    @PostMapping("/from-estimate/{estimateId}")
    public Invoice createFromEstimate(@PathVariable Long estimateId) {
        return invoiceService.createFromEstimate(estimateId);
    }

    @PostMapping("/{id}/payments")
    public Payment pay(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String method = body.get("paymentMethod") != null ? body.get("paymentMethod").toString() : "CASH";
        String ref = body.get("reference") != null ? body.get("reference").toString() : null;
        return invoiceService.recordPayment(id, amount, method, ref);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) throws Exception {
        Invoice invoice = invoiceService.findById(id);
        byte[] pdf = reportService.generateInvoicePdf(invoice);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> excel(@PathVariable Long id) throws Exception {
        Invoice invoice = invoiceService.findById(id);
        byte[] xlsx = reportService.generateInvoiceExcel(invoice);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoice.getInvoiceNumber() + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }
}
