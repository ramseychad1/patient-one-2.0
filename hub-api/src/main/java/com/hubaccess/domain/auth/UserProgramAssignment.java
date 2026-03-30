package com.hubaccess.domain.auth;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_program_assignment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgramAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "can_create_cases", nullable = false)
    private Boolean canCreateCases;

    @Column(name = "can_edit_cases", nullable = false)
    private Boolean canEditCases;

    @Column(name = "can_close_cases", nullable = false)
    private Boolean canCloseCases;

    @Column(name = "can_view_financials", nullable = false)
    private Boolean canViewFinancials;

    @Column(name = "can_perform_actions", nullable = false)
    private Boolean canPerformActions;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) assignedAt = OffsetDateTime.now();
        if (canCreateCases == null) canCreateCases = true;
        if (canEditCases == null) canEditCases = true;
        if (canCloseCases == null) canCloseCases = false;
        if (canViewFinancials == null) canViewFinancials = true;
        if (canPerformActions == null) canPerformActions = true;
    }
}
