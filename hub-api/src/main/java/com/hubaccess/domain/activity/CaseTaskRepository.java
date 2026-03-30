package com.hubaccess.domain.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CaseTaskRepository extends JpaRepository<CaseTask, UUID> {
    List<CaseTask> findByCaseId(UUID caseId);
    List<CaseTask> findByAssignedToAndStatusIn(UUID userId, List<String> statuses);
}
