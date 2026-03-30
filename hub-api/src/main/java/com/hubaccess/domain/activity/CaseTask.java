package com.hubaccess.domain.activity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "task_type", nullable = false, length = 30)
    private String taskType;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 15)
    private String status;

    @Column(nullable = false, length = 10)
    private String priority;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

    @Column(name = "sla_breached", nullable = false)
    private Boolean slaBreached;

    @Column(name = "action_key", length = 100)
    private String actionKey;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "completed_by")
    private UUID completedBy;

    @Column(name = "completion_notes", length = 500)
    private String completionNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = "OPEN";
        if (priority == null) priority = "MEDIUM";
        if (slaBreached == null) slaBreached = false;
    }
}
