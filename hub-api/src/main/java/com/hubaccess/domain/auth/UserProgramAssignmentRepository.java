package com.hubaccess.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProgramAssignmentRepository extends JpaRepository<UserProgramAssignment, UUID> {
    List<UserProgramAssignment> findByUserId(UUID userId);
    List<UserProgramAssignment> findByProgramId(UUID programId);
    Optional<UserProgramAssignment> findByUserIdAndProgramId(UUID userId, UUID programId);
}
