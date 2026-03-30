package com.hubaccess.domain.workflow;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(nullable = false, length = 30)
    private String state;

    @Column(name = "entered_at", nullable = false)
    private OffsetDateTime enteredAt;

    @Column(name = "exited_at")
    private OffsetDateTime exitedAt;

    @Column(name = "triggered_by_action", length = 100)
    private String triggeredByAction;

    @Column(name = "triggered_by_user")
    private UUID triggeredByUser;

    @Column(name = "triggered_by_stub", length = 100)
    private String triggeredByStub;

    @Column(name = "next_required_action", length = 100)
    private String nextRequiredAction;

    @Column(name = "next_action_label", length = 200)
    private String nextActionLabel;

    @Column(name = "next_action_deadline")
    private OffsetDateTime nextActionDeadline;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;

    @PrePersist
    protected void onCreate() {
        if (enteredAt == null) enteredAt = OffsetDateTime.now();
        if (isCurrent == null) isCurrent = true;
    }
}
