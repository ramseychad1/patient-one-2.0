package com.hubaccess.domain.childcase;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BiCaseRepository extends JpaRepository<BiCase, UUID> {
    List<BiCase> findByHubCaseId(UUID hubCaseId);
}
