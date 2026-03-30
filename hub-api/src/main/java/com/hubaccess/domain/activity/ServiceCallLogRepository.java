package com.hubaccess.domain.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ServiceCallLogRepository extends JpaRepository<ServiceCallLog, UUID> {
    List<ServiceCallLog> findByCaseIdOrderByCalledAtDesc(UUID caseId);
}
