package com.hubaccess.domain.childcase;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CopayCaseRepository extends JpaRepository<CopayCase, UUID> {
    List<CopayCase> findByHubCaseId(UUID hubCaseId);
}
