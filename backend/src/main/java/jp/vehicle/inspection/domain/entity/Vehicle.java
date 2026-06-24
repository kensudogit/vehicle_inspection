package jp.vehicle.inspection.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @Column(name = "registration_number", nullable = false, unique = true, length = 20)
    private String registrationNumber;

    @Column(name = "chassis_number", nullable = false, unique = true, length = 17)
    private String chassisNumber;

    private String maker;
    private String model;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(name = "engine_displacement")
    private Integer engineDisplacement;

    @Column(name = "fuel_type", length = 20)
    private String fuelType;

    private String color;

    @Column(name = "first_registration")
    private LocalDate firstRegistration;

    @Column(name = "inspection_expiry", nullable = false)
    private LocalDate inspectionExpiry;

    @Builder.Default
    private Integer mileage = 0;

    @Builder.Default
    private boolean active = true;
}
