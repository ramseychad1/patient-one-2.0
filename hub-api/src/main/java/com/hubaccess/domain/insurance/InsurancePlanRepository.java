package com.hubaccess.domain.insurance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, UUID> {
    List<InsurancePlan> findByCaseId(UUID caseId);
}
