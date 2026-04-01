package com.hubaccess.domain.childcase;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ae_case")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AeCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hub_case_id", nullable = false)
    private UUID hubCaseId;

    @Column(name = "ae_case_number", length = 50)
    private String aeCaseNumber;

    @Column(name = "ae_type", nullable = false, length = 50)
    private String aeType;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "submission_status", length = 50)
    private String submissionStatus;

    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    @Column(name = "event_description", columnDefinition = "TEXT")
    private String eventDescription;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "manufacturer_notified_at")
    private OffsetDateTime manufacturerNotifiedAt;

    @Column(name = "sla_1bd_deadline")
    private OffsetDateTime sla1bdDeadline;

    @Column(name = "sla_3cd_deadline")
    private OffsetDateTime sla3cdDeadline;

    @Column(name = "sla_breached")
    private Boolean slaBreached;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
