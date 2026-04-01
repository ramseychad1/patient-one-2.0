package com.hubaccess.domain.childcase;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AeCaseRepository extends JpaRepository<AeCase, UUID> {
    List<AeCase> findByHubCaseId(UUID hubCaseId);
}
