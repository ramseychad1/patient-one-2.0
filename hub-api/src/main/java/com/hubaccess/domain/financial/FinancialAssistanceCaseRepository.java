package com.hubaccess.domain.financial;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FinancialAssistanceCaseRepository extends JpaRepository<FinancialAssistanceCase, UUID> {
    List<FinancialAssistanceCase> findByCaseId(UUID caseId);
}
