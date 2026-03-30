package com.hubaccess.domain.program;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProgramConfigRepository extends JpaRepository<ProgramConfig, UUID> {
    Optional<ProgramConfig> findByProgramId(UUID programId);
}
