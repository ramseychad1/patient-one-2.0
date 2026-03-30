package com.hubaccess.domain.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findByLastNameAndFirstNameAndDateOfBirth(String lastName, String firstName, LocalDate dob);
}
