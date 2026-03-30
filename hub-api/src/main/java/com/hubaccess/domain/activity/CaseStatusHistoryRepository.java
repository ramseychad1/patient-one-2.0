package com.hubaccess.domain.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CaseStatusHistoryRepository extends JpaRepository<CaseStatusHistory, UUID> {
    List<CaseStatusHistory> findByCaseIdOrderByChangedAtDesc(UUID caseId);
}
