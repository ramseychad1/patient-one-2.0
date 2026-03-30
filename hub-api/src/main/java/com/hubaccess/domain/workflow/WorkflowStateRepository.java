package com.hubaccess.domain.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowStateRepository extends JpaRepository<WorkflowState, UUID> {
    Optional<WorkflowState> findByCaseIdAndIsCurrentTrue(UUID caseId);
    List<WorkflowState> findByCaseIdOrderByEnteredAtDesc(UUID caseId);

    @Modifying
    @Query("UPDATE WorkflowState ws SET ws.isCurrent = false, ws.exitedAt = CURRENT_TIMESTAMP WHERE ws.caseId = :caseId AND ws.isCurrent = true")
    void markCurrentExited(UUID caseId);
}
