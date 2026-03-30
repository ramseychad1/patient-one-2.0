package com.hubaccess.domain.cases;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface HubCaseRepository extends JpaRepository<HubCase, UUID> {
    Optional<HubCase> findByCaseNumber(String caseNumber);
    Page<HubCase> findByAssignedCmId(UUID cmId, Pageable pageable);
    Page<HubCase> findByProgramId(UUID programId, Pageable pageable);
}
