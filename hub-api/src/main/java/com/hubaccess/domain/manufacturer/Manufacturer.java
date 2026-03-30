package com.hubaccess.domain.manufacturer;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "manufacturer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "primary_contact_name", length = 200)
    private String primaryContactName;

    @Column(name = "primary_contact_email", length = 200)
    private String primaryContactEmail;

    @Column(name = "primary_contact_phone", length = 20)
    private String primaryContactPhone;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "contract_reference", length = 200)
    private String contractReference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = "ACTIVE";
    }
}
