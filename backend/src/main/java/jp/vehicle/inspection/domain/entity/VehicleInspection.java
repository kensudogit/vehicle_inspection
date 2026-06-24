package jp.vehicle.inspection.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "vehicle_inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "inspection_type", nullable = false, length = 30)
    @Builder.Default
    private String inspectionType = "REGULAR";

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String result = "PASS";

    @Column(name = "inspector_name", length = 100)
    private String inspectorName;

    @Column(name = "mileage_at_inspection")
    private Integer mileageAtInspection;

    @Column(name = "electronic_cert_id", length = 100)
    private String electronicCertId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "electronic_data", columnDefinition = "jsonb")
    private Map<String, Object> electronicData;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;
}
