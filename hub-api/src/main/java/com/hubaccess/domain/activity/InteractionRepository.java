package com.hubaccess.domain.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {
    List<Interaction> findByCaseIdOrderByCreatedAtDesc(UUID caseId);
}
