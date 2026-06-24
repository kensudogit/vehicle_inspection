package jp.vehicle.inspection.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", nullable = false, unique = true, length = 20)
    private String customerCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_kana", length = 100)
    private String nameKana;

    private String email;
    private String phone;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    private String address;

    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "consent_marketing", nullable = false)
    @Builder.Default
    private boolean consentMarketing = false;
}
