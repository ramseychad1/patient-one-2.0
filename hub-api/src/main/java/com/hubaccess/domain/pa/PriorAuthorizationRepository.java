package com.hubaccess.domain.pa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PriorAuthorizationRepository extends JpaRepository<PriorAuthorization, UUID> {
    List<PriorAuthorization> findByCaseIdOrderByAttemptNumberDesc(UUID caseId);
}
