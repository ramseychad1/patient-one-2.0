package com.hubaccess.domain.childcase;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "missing_information")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hub_case_id", nullable = false)
    private UUID hubCaseId;

    @Column(name = "mi_number", length = 50)
    private String miNumber;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "detail", nullable = false, length = 200)
    private String detail;

    @Column(name = "mi_type", nullable = false, length = 20)
    private String miType;

    @Column(name = "reported_date", nullable = false)
    private LocalDate reportedDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
