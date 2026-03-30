package com.hubaccess.domain.activity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "interaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "interaction_type", nullable = false, length = 25)
    private String interactionType;

    @Column(length = 10)
    private String direction;

    @Column(length = 10)
    private String channel;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "contact_role", length = 15)
    private String contactRole;

    @Column(length = 300)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "adverse_event_reported", nullable = false)
    private Boolean adverseEventReported;

    @Column(name = "adverse_event_detail", columnDefinition = "TEXT")
    private String adverseEventDetail;

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (adverseEventReported == null) adverseEventReported = false;
    }
}
