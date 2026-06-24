package jp.vehicle.inspection.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inspection_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String result = "OK";

    @Column(name = "measured_value", length = 50)
    private String measuredValue;

    @Column(name = "standard_value", length = 50)
    private String standardValue;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
