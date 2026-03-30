package com.hubaccess.domain.prescriber;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PrescriberRepository extends JpaRepository<Prescriber, UUID> {
    Optional<Prescriber> findByNpi(String npi);
}
