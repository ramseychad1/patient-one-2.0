package com.hubaccess.domain.insurance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BenefitsVerificationRepository extends JpaRepository<BenefitsVerification, UUID> {
    List<BenefitsVerification> findByCaseId(UUID caseId);
}
