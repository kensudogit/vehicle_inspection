package jp.vehicle.inspection.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "maintenance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "performed_at", nullable = false)
    private OffsetDateTime performedAt;

    @Column(name = "work_type", nullable = false, length = 50)
    private String workType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private Integer mileage;

    @Column(name = "technician_id")
    private Long technicianId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parts_used", columnDefinition = "jsonb")
    private List<Map<String, Object>> partsUsed;

    @Column(name = "labor_hours", precision = 5, scale = 2)
    private BigDecimal laborHours;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "previous_hash", length = 64)
    private String previousHash;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean locked = false;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;
}
