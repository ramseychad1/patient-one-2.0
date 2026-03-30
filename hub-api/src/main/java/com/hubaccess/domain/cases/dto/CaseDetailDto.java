package com.hubaccess.domain.cases.dto;

import com.hubaccess.domain.activity.dto.CaseTaskDto;
import com.hubaccess.domain.activity.dto.TimelineEntryDto;
import com.hubaccess.domain.financial.dto.FinancialAssistanceDto;
import com.hubaccess.domain.insurance.dto.BenefitsVerificationDto;
import com.hubaccess.domain.insurance.dto.InsurancePlanDto;
import com.hubaccess.domain.pa.dto.PriorAuthorizationDto;
import com.hubaccess.domain.patient.dto.PatientDto;
import com.hubaccess.domain.prescriber.dto.PrescriberDto;
import com.hubaccess.domain.workflow.dto.WorkflowStateDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CaseDetailDto(
        UUID id,
        String caseNumber,
        String programName,
        String stage,
        String workflowState,
        String sourceChannel,
        String insuranceType,
        Boolean paRequired,
        String paStatus,
        Boolean copayEligible,
        Boolean papEligible,
        String papStatus,
        Boolean bridgeActive,
        Boolean quickStartActive,
        Boolean slaBreachFlag,
        Boolean escalationFlag,
        String escalationReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        PatientDto patient,
        PrescriberDto prescriber,
        InsurancePlanDto insurancePlan,
        BenefitsVerificationDto benefitsVerification,
        WorkflowStateDto workflowStateDetail,
        List<CaseTaskDto> openTasks,
        List<TimelineEntryDto> recentTimeline,
        List<PriorAuthorizationDto> priorAuthorizations,
        List<FinancialAssistanceDto> financialAssistance
) {}
