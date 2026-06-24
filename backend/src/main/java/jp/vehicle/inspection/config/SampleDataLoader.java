package jp.vehicle.inspection.config;

import jp.vehicle.inspection.domain.entity.*;
import jp.vehicle.inspection.domain.repository.*;
import jp.vehicle.inspection.util.MaintenanceHashUtil;
import jp.vehicle.inspection.util.VehicleValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 初回起動時（顧客データが空のとき）にデモ用サンプルデータを投入する。
 */
@Slf4j
@Component
@Order(2)
public class SampleDataLoader implements CommandLineRunner {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final InspectionReservationRepository reservationRepository;
    private final EstimateRepository estimateRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final NotificationRepository notificationRepository;
    private final VehicleInspectionRepository vehicleInspectionRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final MaintenanceHashUtil hashUtil;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.seed-sample-data:true}")
    private boolean seedEnabled;

    public SampleDataLoader(
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            InspectionReservationRepository reservationRepository,
            EstimateRepository estimateRepository,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            MaintenanceRecordRepository maintenanceRecordRepository,
            NotificationRepository notificationRepository,
            VehicleInspectionRepository vehicleInspectionRepository,
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            MaintenanceHashUtil hashUtil,
            PlatformTransactionManager transactionManager) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
        this.estimateRepository = estimateRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.notificationRepository = notificationRepository;
        this.vehicleInspectionRepository = vehicleInspectionRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.hashUtil = hashUtil;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        if (customerRepository.count() > 0) {
            return;
        }
        try {
            transactionTemplate.executeWithoutResult(status -> seed());
        } catch (Exception e) {
            log.error("Sample data seed failed; application will continue without demo data", e);
        }
    }

    private void seed() {
        Long adminId = userRepository.findByEmail("admin@vehicle-inspection.local")
                .map(User::getId)
                .orElse(null);
        OffsetDateTime now = OffsetDateTime.now();
        LocalDate today = LocalDate.now();

        Customer yamada = saveCustomer("C-DEMO-0001", "山田 太郎", "ヤマダタロウ",
                "yamada@example.com", "090-1111-2222", "150-0001", "東京都渋谷区1-1-1", adminId);
        Customer sato = saveCustomer("C-DEMO-0002", "佐藤 花子", "サトウハナコ",
                "sato@example.com", "090-3333-4444", "220-0001", "神奈川県横浜市2-2-2", adminId);
        Customer suzuki = saveCustomer("C-DEMO-0003", "鈴木 一郎", "スズキイチロウ",
                "suzuki@example.com", "080-5555-6666", "190-0001", "東京都立川市3-3-3", adminId);

        Vehicle v1 = saveVehicle(yamada.getId(), "品川500あ1234", "JTDBT923503012345",
                "トヨタ", "プリウス", today.plusDays(14), 45200, adminId);
        Vehicle v2 = saveVehicle(sato.getId(), "横浜330い5678", "JH4NA16555T012345",
                "ホンダ", "フィット", today.plusDays(45), 32100, adminId);
        Vehicle v3 = saveVehicle(suzuki.getId(), "多摩580う9012", "WBA3A31070F123456",
                "BMW", "320i", today.plusDays(7), 67800, adminId);

        saveReservation(v1, yamada, now.plusDays(3), "INSPECTION", "SCHEDULED");
        saveReservation(v2, sato, now.plusDays(10), "MAINTENANCE", "SCHEDULED");
        saveReservation(v3, suzuki, now.plusDays(1), "INSPECTION", "CONFIRMED");

        Estimate est1 = saveEstimate("EST-DEMO-001", v1, yamada, adminId, now,
                List.of(line("車検基本料金", "50000"), line("重量税", "16400"), line("自賠責保険", "17650")));
        Estimate est2 = saveEstimate("EST-DEMO-002", v2, sato, adminId, now,
                List.of(line("12ヶ月点検", "12000"), line("エンジンオイル交換", "8800")));
        saveEstimate("EST-DEMO-003", v3, suzuki, adminId, now,
                List.of(line("車検基本料金", "55000"), line("ブレーキパッド交換", "22000")));

        Invoice inv1 = saveInvoice("INV-DEMO-001", est1, v1, yamada, "UNPAID", adminId, now);
        Invoice inv2 = saveInvoice("INV-DEMO-002", est2, v2, sato, "PAID", adminId, now);
        savePayment(inv2, inv2.getTotalAmount(), "CARD", adminId, now.minusDays(2));

        saveMaintenanceChain(v1.getId(), adminId, now);
        saveMaintenanceChain(v2.getId(), adminId, now.minusDays(30));

        saveNotification(yamada, v1, "EMAIL", "EXPIRY_REMINDER", "yamada@example.com",
                "車検満了のお知らせ（品川500あ1234）", "SENT", now.minusDays(1));
        saveNotification(suzuki, v3, "SMS", "EXPIRY_REMINDER", "080-5555-6666",
                "車検満了7日前のお知らせ", "PENDING", now.plusHours(2));
        saveNotification(sato, v2, "EMAIL", "EXPIRY_REMINDER", "sato@example.com",
                "車検満了45日前のお知らせ", "PENDING", now.plusDays(1));

        saveVehicleInspection(v1, today.minusYears(2), today.plusDays(14),
                "PASS", "E-CERT-DEMO-001", adminId, now);

        saveAuditLog(adminId, "SEED", "SYSTEM", null, now.minusMinutes(5));
        saveAuditLog(adminId, "CREATE", "CUSTOMER", yamada.getId(), now.minusMinutes(4));
        saveAuditLog(adminId, "CREATE", "VEHICLE", v1.getId(), now.minusMinutes(3));
        saveAuditLog(adminId, "CREATE", "INVOICE", inv1.getId(), now.minusMinutes(1));

        log.info("Sample data loaded: {} customers, {} vehicles, {} invoices",
                customerRepository.count(), vehicleRepository.count(), invoiceRepository.count());
    }

    private Customer saveCustomer(String code, String name, String kana, String email, String phone,
                                  String postal, String address, Long adminId) {
        Customer c = Customer.builder()
                .customerCode(code)
                .name(name)
                .nameKana(kana)
                .email(email)
                .phone(phone)
                .postalCode(postal)
                .address(address)
                .consentMarketing(false)
                .build();
        c.setCreatedBy(adminId);
        c.setUpdatedBy(adminId);
        return customerRepository.save(c);
    }

    private Vehicle saveVehicle(Long customerId, String reg, String chassis, String maker, String model,
                                LocalDate expiry, int mileage, Long adminId) {
        String normalizedReg = VehicleValidation.normalizeRegistrationNumber(reg);
        String normalizedChassis = VehicleValidation.normalizeChassisNumber(chassis);
        if (!VehicleValidation.isValidRegistrationNumber(normalizedReg)) {
            throw new IllegalArgumentException("Invalid sample registration number: " + reg);
        }
        if (!VehicleValidation.isValidChassisNumber(normalizedChassis)) {
            throw new IllegalArgumentException(
                    "Invalid sample chassis number (must be 17 chars): " + normalizedChassis);
        }
        Vehicle v = Vehicle.builder()
                .customerId(customerId)
                .registrationNumber(normalizedReg)
                .chassisNumber(normalizedChassis)
                .maker(maker)
                .model(model)
                .modelYear(2019)
                .engineDisplacement(1800)
                .fuelType("GASOLINE")
                .color("シルバー")
                .firstRegistration(expiry.minusYears(2))
                .inspectionExpiry(expiry)
                .mileage(mileage)
                .active(true)
                .build();
        v.setCreatedBy(adminId);
        v.setUpdatedBy(adminId);
        return vehicleRepository.save(v);
    }

    private void saveReservation(Vehicle vehicle, Customer customer, OffsetDateTime reservedAt,
                                   String serviceType, String status) {
        reservationRepository.save(InspectionReservation.builder()
                .vehicleId(vehicle.getId())
                .customerId(customer.getId())
                .reservedAt(reservedAt)
                .serviceType(serviceType)
                .status(status)
                .notes("サンプル予約データ")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
    }

    private EstimateItem line(String description, String unitPrice) {
        BigDecimal price = new BigDecimal(unitPrice);
        return EstimateItem.builder()
                .itemType("LABOR")
                .description(description)
                .quantity(BigDecimal.ONE)
                .unitPrice(price)
                .amount(price)
                .build();
    }

    private Estimate saveEstimate(String number, Vehicle vehicle, Customer customer, Long adminId,
                                  OffsetDateTime now, List<EstimateItem> itemTemplates) {
        Estimate estimate = Estimate.builder()
                .estimateNumber(number)
                .vehicleId(vehicle.getId())
                .customerId(customer.getId())
                .status("APPROVED")
                .validUntil(LocalDate.now().plusDays(30))
                .notes("サンプル見積")
                .createdAt(now)
                .updatedAt(now)
                .createdBy(adminId)
                .build();
        for (EstimateItem template : itemTemplates) {
            EstimateItem item = EstimateItem.builder()
                    .estimate(estimate)
                    .itemType(template.getItemType())
                    .description(template.getDescription())
                    .quantity(template.getQuantity())
                    .unitPrice(template.getUnitPrice())
                    .amount(template.getAmount())
                    .build();
            estimate.getItems().add(item);
        }
        recalculate(estimate);
        return estimateRepository.save(estimate);
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

    private Invoice saveInvoice(String number, Estimate estimate, Vehicle vehicle, Customer customer,
                                String status, Long adminId, OffsetDateTime now) {
        return invoiceRepository.save(Invoice.builder()
                .invoiceNumber(number)
                .estimateId(estimate.getId())
                .vehicleId(vehicle.getId())
                .customerId(customer.getId())
                .status(status)
                .subtotal(estimate.getSubtotal())
                .taxAmount(estimate.getTaxAmount())
                .totalAmount(estimate.getTotalAmount())
                .issuedAt(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .notes("サンプル請求書")
                .createdAt(now)
                .updatedAt(now)
                .createdBy(adminId)
                .build());
    }

    private void savePayment(Invoice invoice, BigDecimal amount, String method, Long adminId, OffsetDateTime paidAt) {
        paymentRepository.save(Payment.builder()
                .invoiceId(invoice.getId())
                .amount(amount)
                .paymentMethod(method)
                .paidAt(paidAt)
                .reference("SAMPLE-PAY-001")
                .createdBy(adminId)
                .build());
    }

    private void saveMaintenanceChain(Long vehicleId, Long adminId, OffsetDateTime baseTime) {
        MaintenanceRecord first = MaintenanceRecord.builder()
                .vehicleId(vehicleId)
                .performedAt(baseTime.minusMonths(6))
                .workType("OIL_CHANGE")
                .description("エンジンオイル・オイルフィルター交換（サンプル）")
                .mileage(42000)
                .laborHours(new BigDecimal("1.0"))
                .previousHash(null)
                .locked(true)
                .lockedAt(baseTime.minusMonths(6))
                .createdAt(baseTime.minusMonths(6))
                .updatedAt(baseTime.minusMonths(6))
                .createdBy(adminId)
                .build();
        first.setContentHash(hashUtil.computeHash(first));
        maintenanceRecordRepository.save(first);

        MaintenanceRecord second = MaintenanceRecord.builder()
                .vehicleId(vehicleId)
                .performedAt(baseTime.minusDays(14))
                .workType("INSPECTION")
                .description("車検前点検・ブレーキパッド残量確認（サンプル）")
                .mileage(45200)
                .laborHours(new BigDecimal("2.5"))
                .previousHash(first.getContentHash())
                .locked(false)
                .createdAt(baseTime.minusDays(14))
                .updatedAt(baseTime.minusDays(14))
                .createdBy(adminId)
                .build();
        second.setContentHash(hashUtil.computeHash(second));
        maintenanceRecordRepository.save(second);
    }

    private void saveNotification(Customer customer, Vehicle vehicle, String channel, String type,
                                  String recipient, String subject, String status, OffsetDateTime scheduledAt) {
        notificationRepository.save(Notification.builder()
                .customerId(customer.getId())
                .vehicleId(vehicle.getId())
                .channel(channel)
                .notificationType(type)
                .recipient(recipient)
                .subject(subject)
                .body(subject + " — サンプル通知本文。車検満了日をご確認ください。")
                .status(status)
                .scheduledAt(scheduledAt)
                .sentAt("SENT".equals(status) ? scheduledAt : null)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private void saveVehicleInspection(Vehicle vehicle, LocalDate inspectionDate, LocalDate expiry,
                                       String result, String certId, Long adminId, OffsetDateTime now) {
        vehicleInspectionRepository.save(VehicleInspection.builder()
                .vehicleId(vehicle.getId())
                .inspectionType("ELECTRONIC")
                .inspectionDate(inspectionDate)
                .expiryDate(expiry)
                .result(result)
                .inspectorName("検査員 デモ")
                .mileageAtInspection(45000)
                .electronicCertId(certId)
                .electronicData(Map.of(
                        "registrationNumber", vehicle.getRegistrationNumber(),
                        "chassisNumber", vehicle.getChassisNumber(),
                        "source", "SAMPLE"
                ))
                .notes("サンプル電子車検証データ")
                .createdAt(now)
                .updatedAt(now)
                .createdBy(adminId)
                .build());
    }

    private void saveAuditLog(Long userId, String action, String entityType, Long entityId, OffsetDateTime createdAt) {
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .newValue(Map.of("source", "sample-data-loader"))
                .ipAddress("127.0.0.1")
                .userAgent("SampleDataLoader")
                .createdAt(createdAt)
                .build());
    }
}
