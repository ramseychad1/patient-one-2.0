package com.hubaccess.domain.prescriber;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescriber")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prescriber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String npi;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "practice_name", length = 200)
    private String practiceName;

    @Column(length = 200)
    private String specialty;

    @Column(name = "address_line1", length = 200)
    private String addressLine1;

    @Column(length = 100)
    private String city;

    @Column(length = 2)
    private String state;

    @Column(length = 10)
    private String zip;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String fax;

    @Column(length = 200)
    private String email;

    @Column(name = "dea_number", length = 20)
    private String deaNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
