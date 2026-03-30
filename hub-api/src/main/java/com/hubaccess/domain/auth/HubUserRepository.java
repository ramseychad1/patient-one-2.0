package com.hubaccess.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface HubUserRepository extends JpaRepository<HubUser, UUID> {
    Optional<HubUser> findByEmail(String email);
}
