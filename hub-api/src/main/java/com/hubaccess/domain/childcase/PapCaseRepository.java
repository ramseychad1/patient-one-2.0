package com.hubaccess.domain.childcase;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PapCaseRepository extends JpaRepository<PapCase, UUID> {
    List<PapCase> findByHubCaseId(UUID hubCaseId);
}
