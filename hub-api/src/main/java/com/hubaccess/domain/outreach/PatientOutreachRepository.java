package com.hubaccess.domain.outreach;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PatientOutreachRepository extends JpaRepository<PatientOutreach, UUID> {
    List<PatientOutreach> findByCaseId(UUID caseId);
}
