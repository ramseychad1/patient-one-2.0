package com.hubaccess.domain.enrollment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EnrollmentRequest(
        @NotBlank String enrollmentSource,
        @NotNull UUID programId,
        @NotNull PatientInfo patient,
        PrescriberInfo prescriber,
        DrugInfo drug,
        InsuranceInfo insurance,
        List<String> miFlags,
        String erxTransactionId
) {
    public record PatientInfo(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotNull LocalDate dob,
            String phone,
            String preferredContactMethod
    ) {}

    public record PrescriberInfo(
            @NotBlank String npi,
            String firstName,
            String lastName,
            String practiceName,
            String phone,
            String fax
    ) {}

    public record DrugInfo(
            String ndcCode,
            String brandName,
            String diagnosisCode
    ) {}

    public record InsuranceInfo(
            String insuranceType,
            String planName,
            String memberId,
            String groupNumber
    ) {}
}
