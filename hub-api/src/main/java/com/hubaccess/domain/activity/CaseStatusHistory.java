package com.hubaccess.domain.activity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_status_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "from_state", length = 30)
    private String fromState;

    @Column(name = "to_state", nullable = false, length = 30)
    private String toState;

    @Column(name = "from_stage", length = 20)
    private String fromStage;

    @Column(name = "to_stage", length = 20)
    private String toStage;

    @Column(name = "triggered_by_action", length = 100)
    private String triggeredByAction;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) changedAt = OffsetDateTime.now();
    }
}
