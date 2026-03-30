package com.hubaccess.domain.program;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "program")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "manufacturer_id", nullable = false)
    private UUID manufacturerId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "drug_brand_name", nullable = false, length = 200)
    private String drugBrandName;

    @Column(name = "drug_generic_name", length = 200)
    private String drugGenericName;

    @Column(name = "ndc_codes", columnDefinition = "VARCHAR[]")
    private String[] ndcCodes;

    @Column(name = "therapeutic_area", length = 200)
    private String therapeuticArea;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "program_start_date")
    private LocalDate programStartDate;

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
