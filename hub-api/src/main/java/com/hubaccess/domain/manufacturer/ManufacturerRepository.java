package com.hubaccess.domain.manufacturer;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, UUID> {
    Optional<Manufacturer> findByName(String name);
}
