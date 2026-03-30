package com.hubaccess.domain.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRecordRepository extends JpaRepository<EnrollmentRecord, UUID> {
    Optional<EnrollmentRecord> findByCaseId(UUID caseId);
}
