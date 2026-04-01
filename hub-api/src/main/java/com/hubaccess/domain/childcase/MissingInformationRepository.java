package com.hubaccess.domain.childcase;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MissingInformationRepository extends JpaRepository<MissingInformation, UUID> {
    List<MissingInformation> findByHubCaseId(UUID hubCaseId);
}
