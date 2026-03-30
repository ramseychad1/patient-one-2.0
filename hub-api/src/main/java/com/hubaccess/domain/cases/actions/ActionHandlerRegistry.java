package com.hubaccess.domain.cases.actions;

import com.hubaccess.domain.activity.CaseTaskRepository;
import com.hubaccess.domain.activity.Interaction;
import com.hubaccess.domain.activity.InteractionRepository;
import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.cases.dto.ActionResultDto;
import com.hubaccess.domain.enrollment.EnrollmentRecord;
import com.hubaccess.domain.enrollment.EnrollmentRecordRepository;
import com.hubaccess.domain.financial.FinancialAssistanceService;
import com.hubaccess.domain.financial.dto.FaEligibilityResult;
import com.hubaccess.domain.financial.dto.FinancialAssistanceDto;
import com.hubaccess.domain.insurance.InsurancePlan;
import com.hubaccess.domain.insurance.InsurancePlanRepository;
import com.hubaccess.domain.insurance.BenefitsVerification;
import com.hubaccess.domain.insurance.BenefitsVerificationRepository;
import com.hubaccess.domain.pa.PriorAuthorizationService;
import com.hubaccess.domain.pa.dto.CreatePaRequest;
import com.hubaccess.security.AuthenticatedUser;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ActionHandlerRegistry {

    private final ActionService actionService;
    private final InteractionRepository interactionRepo;
    private final EnrollmentRecordRepository enrollmentRepo;
    private final CaseTaskRepository caseTaskRepo;
    private final InsurancePlanRepository insurancePlanRepo;
    private final BenefitsVerificationRepository bvRepo;
    private final FinancialAssistanceService faService;
    private final PriorAuthorizationService paService;

    @PostConstruct
    public void registerAll() {
        actionService.registerHandler("RESOLVE_MI", new ResolveMiHandler());
        actionService.registerHandler("SEND_CONSENT_SMS", new SendConsentSmsHandler());
        actionService.registerHandler("CONFIRM_CONSENT", new ConfirmConsentHandler());
        actionService.registerHandler("RUN_EBV", new RunEbvHandler());
        actionService.registerHandler("GENERATE_SOB_FAX", new GenerateSobFaxHandler());
        actionService.registerHandler("GENERATE_PA_PACKAGE", new GeneratePaPackageHandler());
        actionService.registerHandler("RECORD_PA_SUBMISSION", new RecordPaSubmissionHandler());
        actionService.registerHandler("CHECK_PA_STATUS", new CheckPaStatusHandler());
        actionService.registerHandler("EVALUATE_FA", new EvaluateFaHandler());
        actionService.registerHandler("ENROLL_COPAY", new EnrollCopayHandler());
        actionService.registerHandler("ROUTE_TO_SP", new RouteToSpHandler());
        actionService.registerHandler("CONFIRM_FIRST_DISPENSE", new ConfirmFirstDispenseHandler());
        actionService.registerHandler("SEND_REFILL_REMINDER", new SendRefillReminderHandler());
        actionService.registerHandler("LOG_CHECKIN_CALL", new LogCheckinCallHandler());
        actionService.registerHandler("RECORD_PA_DECISION", new RecordPaDecisionHandler());
        actionService.registerHandler("SUBMIT_PA_APPEAL", new SubmitPaAppealHandler());
        actionService.registerHandler("ENROLL_PAP", new EnrollPapHandler());
        actionService.registerHandler("ENROLL_BRIDGE", new EnrollBridgeHandler());
    }

    private void simulateLatency(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs + (long) (Math.random() * (maxMs - minMs)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ─── Handlers ────────────────────────────────────────────────────

    class ResolveMiHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            // Mark MI as resolved on the enrollment record
            enrollmentRepo.findByCaseId(hc.getId()).ifPresent(er -> {
                er.setMiResolvedAt(OffsetDateTime.now());
                er.setMiResolvedBy(user.id());
                enrollmentRepo.save(er);
            });
            // Complete all RESOLVE_MI tasks for this case
            caseTaskRepo.findByCaseId(hc.getId()).stream()
                    .filter(t -> "RESOLVE_MI".equals(t.getTaskType()) && "OPEN".equals(t.getStatus()))
                    .forEach(t -> {
                        t.setStatus("COMPLETED");
                        t.setCompletedAt(OffsetDateTime.now());
                        t.setCompletedBy(user.id());
                        caseTaskRepo.save(t);
                    });
            return null;
        }
        @Override public String getNextState(HubCase hc) { return "CONSENT_PENDING"; }
        @Override public String getNextStage(HubCase hc) { return "CONSENT"; }
        @Override public String getNextActionKey(HubCase hc) { return "SEND_CONSENT_SMS"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Send consent request"; }
    }

    class SendConsentSmsHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(500, 1000);
            String smsId = "SMS-" + UUID.randomUUID().toString().substring(0, 8);
            return new ActionResultDto.StubResult("SmsService", 750, Map.of(
                    "sms_id", smsId,
                    "recipient", "patient_phone",
                    "status", "SENT (mock)",
                    "consent_url", "https://consent.hubaccess.demo/c/" + UUID.randomUUID().toString().substring(0, 8)
            ));
        }
        @Override public String getNextState(HubCase hc) { return "CONSENT_PENDING"; }
        @Override public String getNextStage(HubCase hc) { return "CONSENT"; }
        @Override public String getNextActionKey(HubCase hc) { return "CONFIRM_CONSENT"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Confirm consent received"; }
    }

    class ConfirmConsentHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            enrollmentRepo.findByCaseId(hc.getId()).ifPresent(er -> {
                er.setConsentHipaa(true);
                er.setConsentProgram(true);
                er.setConsentCollectedAt(OffsetDateTime.now());
                er.setConsentCollectedMethod("SMS_PORTAL");
                enrollmentRepo.save(er);
            });
            return null;
        }
        @Override public String getNextState(HubCase hc) { return "BI_PENDING"; }
        @Override public String getNextStage(HubCase hc) { return "BI_BV"; }
        @Override public String getNextActionKey(HubCase hc) { return "RUN_EBV"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Run BI/BV"; }
    }

    class RunEbvHandler implements ActionHandler {
        private Map<String, String> resultFields;

        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(800, 1500);
            String scenario = actionService.getDemoScenario();

            resultFields = switch (scenario) {
                case "GOVERNMENT_PATIENT" -> buildGovernmentResult(hc);
                case "UNINSURED_PATIENT" -> buildUninsuredResult(hc);
                default -> buildCommercialResult(hc);
            };

            // Create insurance plan record
            String insType = resultFields.get("insurance_type");
            InsurancePlan ip = InsurancePlan.builder()
                    .caseId(hc.getId())
                    .insuranceType(insType.equals("COMMERCIAL") ? "COMMERCIAL" : insType.equals("GOVERNMENT") ? "MEDICARE" : "UNINSURED")
                    .planName(resultFields.get("plan_name"))
                    .payerName(resultFields.getOrDefault("payer_name", ""))
                    .planType(resultFields.getOrDefault("plan_type", ""))
                    .paRequired("true".equals(resultFields.get("pa_required")))
                    .formularyTier(resultFields.containsKey("formulary_tier") ? Integer.parseInt(resultFields.get("formulary_tier")) : null)
                    .copayDrug(resultFields.containsKey("copay_drug") ? new BigDecimal(resultFields.get("copay_drug")) : null)
                    .deductibleIndividual(resultFields.containsKey("deductible") ? new BigDecimal(resultFields.get("deductible")) : null)
                    .isPrimary(true)
                    .ebvSource("SURESCRIPTS_MOCK")
                    .ebvRunAt(OffsetDateTime.now())
                    .build();
            insurancePlanRepo.save(ip);

            // Create BV record
            bvRepo.save(BenefitsVerification.builder()
                    .caseId(hc.getId())
                    .insurancePlanId(ip.getId())
                    .verificationType("EBV")
                    .status("COMPLETE")
                    .performedBy(user.id())
                    .completedAt(OffsetDateTime.now())
                    .build());

            // Update case insurance type
            hc.setInsuranceType(insType);
            hc.setPaRequired("true".equals(resultFields.get("pa_required")));

            return new ActionResultDto.StubResult("EbvService", 1243, resultFields);
        }

        private Map<String, String> buildCommercialResult(HubCase hc) {
            return new LinkedHashMap<>(Map.of(
                    "insurance_type", "COMMERCIAL",
                    "plan_name", "BlueCross PPO Gold",
                    "payer_name", "BlueCross BlueShield",
                    "plan_type", "PPO",
                    "pa_required", "true",
                    "copay_drug", "50.00",
                    "formulary_tier", "3",
                    "deductible", "1500.00",
                    "covered_sps", "CVS Specialty, Walgreens Specialty"
            ));
        }

        private Map<String, String> buildGovernmentResult(HubCase hc) {
            return new LinkedHashMap<>(Map.of(
                    "insurance_type", "GOVERNMENT",
                    "plan_name", "Medicare Part D",
                    "payer_name", "CMS",
                    "plan_type", "Medicare",
                    "pa_required", "false",
                    "copay_drug", "0.00",
                    "formulary_tier", "2"
            ));
        }

        private Map<String, String> buildUninsuredResult(HubCase hc) {
            return new LinkedHashMap<>(Map.of(
                    "insurance_type", "UNINSURED",
                    "plan_name", "None",
                    "pa_required", "false"
            ));
        }

        @Override
        public String getNextState(HubCase hc) {
            if ("COMMERCIAL".equals(hc.getInsuranceType()) && Boolean.TRUE.equals(hc.getPaRequired())) return "PA_PENDING";
            if ("COMMERCIAL".equals(hc.getInsuranceType())) return "COPAY_ASSESSMENT";
            return "PAP_ASSESSMENT";
        }
        @Override
        public String getNextStage(HubCase hc) {
            if ("COMMERCIAL".equals(hc.getInsuranceType()) && Boolean.TRUE.equals(hc.getPaRequired())) return "PA";
            if ("COMMERCIAL".equals(hc.getInsuranceType())) return "FINANCIAL";
            return "FINANCIAL";
        }
        @Override
        public String getNextActionKey(HubCase hc) {
            if ("COMMERCIAL".equals(hc.getInsuranceType()) && Boolean.TRUE.equals(hc.getPaRequired())) return "GENERATE_PA_PACKAGE";
            if ("COMMERCIAL".equals(hc.getInsuranceType())) return "EVALUATE_FA";
            return "EVALUATE_FA";
        }
        @Override
        public String getNextActionLabel(HubCase hc) {
            if ("COMMERCIAL".equals(hc.getInsuranceType()) && Boolean.TRUE.equals(hc.getPaRequired())) return "Generate PA package";
            if ("COMMERCIAL".equals(hc.getInsuranceType())) return "Evaluate financial assistance";
            return "Evaluate financial assistance";
        }
    }

    class GenerateSobFaxHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(600, 1200);
            return new ActionResultDto.StubResult("FaxService", 900, Map.of(
                    "fax_id", "FAX-" + UUID.randomUUID().toString().substring(0, 8),
                    "pages", "3",
                    "destination", "prescriber_fax",
                    "status", "SENT (mock)"
            ));
        }
        @Override public String getNextState(HubCase hc) { return null; }
        @Override public String getNextStage(HubCase hc) { return null; }
        @Override public String getNextActionKey(HubCase hc) { return null; }
        @Override public String getNextActionLabel(HubCase hc) { return null; }
    }

    class GeneratePaPackageHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(700, 1400);
            return new ActionResultDto.StubResult("FaxService", 1100, Map.of(
                    "fax_id", "FAX-" + UUID.randomUUID().toString().substring(0, 8),
                    "document", "PA Package — Cover Letter + ePA Form",
                    "pages", "5",
                    "destination", "prescriber_fax",
                    "status", "SENT (mock)"
            ));
        }
        @Override public String getNextState(HubCase hc) { return "PA_PENDING"; }
        @Override public String getNextStage(HubCase hc) { return "PA"; }
        @Override public String getNextActionKey(HubCase hc) { return "RECORD_PA_SUBMISSION"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Record PA submission"; }
    }

    class RecordPaSubmissionHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            hc.setPaStatus("SUBMITTED");
            return null;
        }
        @Override public String getNextState(HubCase hc) { return "PA_SUBMITTED"; }
        @Override public String getNextStage(HubCase hc) { return "PA"; }
        @Override public String getNextActionKey(HubCase hc) { return "CHECK_PA_STATUS"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Check PA status"; }
    }

    class CheckPaStatusHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(800, 2000);
            String scenario = actionService.getDemoScenario();
            String decision = "PA_DENIED".equals(scenario) ? "DENIED" : "APPROVED";
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("pa_status", decision);
            fields.put("payer", "BlueCross BlueShield");
            fields.put("turnaround_days", "7");
            if ("APPROVED".equals(decision)) {
                fields.put("authorization_number", "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                fields.put("effective_date", java.time.LocalDate.now().toString());
                fields.put("end_date", java.time.LocalDate.now().plusYears(1).toString());
                hc.setPaStatus("APPROVED");
            } else {
                fields.put("denial_reason", "Step therapy requirement not met");
                fields.put("denial_code", "ST-001");
                fields.put("appeal_deadline", java.time.LocalDate.now().plusDays(30).toString());
                hc.setPaStatus("DENIED");
            }
            return new ActionResultDto.StubResult("EpaService", 1450, fields);
        }
        @Override public String getNextState(HubCase hc) {
            return "APPROVED".equals(hc.getPaStatus()) ? "PA_APPROVED" : "PA_DENIED";
        }
        @Override public String getNextStage(HubCase hc) {
            return "APPROVED".equals(hc.getPaStatus()) ? "FINANCIAL" : "PA";
        }
        @Override public String getNextActionKey(HubCase hc) {
            return "APPROVED".equals(hc.getPaStatus()) ? "EVALUATE_FA" : "SUBMIT_PA_APPEAL";
        }
        @Override public String getNextActionLabel(HubCase hc) {
            return "APPROVED".equals(hc.getPaStatus()) ? "Evaluate financial assistance" : "Submit PA appeal";
        }
    }

    class EvaluateFaHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            java.util.List<FaEligibilityResult> results = faService.evaluate(hc.getId(), user);
            Map<String, String> fields = new LinkedHashMap<>();
            for (FaEligibilityResult r : results) {
                fields.put(r.faType().toLowerCase() + "_status", r.status());
                if (r.reason() != null) fields.put(r.faType().toLowerCase() + "_reason", r.reason());
                if (r.reasonCode() != null) fields.put(r.faType().toLowerCase() + "_reason_code", r.reasonCode());
                if (r.incomeFplPct() != null) fields.put("income_fpl_pct", r.incomeFplPct().toString());
            }
            return new ActionResultDto.StubResult("FaEligibilityEngine", 50, fields);
        }

        @Override public String getNextState(HubCase hc) { return "FINANCIAL_COMPLETE"; }
        @Override public String getNextStage(HubCase hc) { return "FINANCIAL"; }
        @Override
        public String getNextActionKey(HubCase hc) {
            if (Boolean.TRUE.equals(hc.getCopayEligible())) return "ENROLL_COPAY";
            if (Boolean.TRUE.equals(hc.getPapEligible())) return "ENROLL_PAP";
            return "ROUTE_TO_SP";
        }
        @Override
        public String getNextActionLabel(HubCase hc) {
            if (Boolean.TRUE.equals(hc.getCopayEligible())) return "Enroll in copay assistance";
            if (Boolean.TRUE.equals(hc.getPapEligible())) return "Enroll in PAP";
            return "Route to specialty pharmacy";
        }
    }

    class EnrollCopayHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(600, 1000);
            FinancialAssistanceDto fa = faService.enrollCopay(hc.getId(), user);
            return new ActionResultDto.StubResult("CopayService", 800, Map.of(
                    "card_number", fa.faType(),
                    "bin", "610020",
                    "pcn", "HUBPAY",
                    "group", "MERIDIAN01",
                    "max_benefit", "$15,000",
                    "status", "ENROLLED"
            ));
        }
        @Override public String getNextState(HubCase hc) { return "TRIAGE_PENDING"; }
        @Override public String getNextStage(HubCase hc) { return "TRIAGE"; }
        @Override public String getNextActionKey(HubCase hc) { return "ROUTE_TO_SP"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Route to specialty pharmacy"; }
    }

    class EnrollPapHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(500, 900);
            faService.enrollPap(hc.getId(), user);
            hc.setPapStatus("APPROVED");
            return new ActionResultDto.StubResult("ShpsService", 700, Map.of(
                    "order_id", "SHPS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    "status", "PAP_APPROVED",
                    "duration_months", "12"
            ));
        }
        @Override public String getNextState(HubCase hc) { return "TRIAGE_PENDING"; }
        @Override public String getNextStage(HubCase hc) { return "TRIAGE"; }
        @Override public String getNextActionKey(HubCase hc) { return "ROUTE_TO_SP"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Route to specialty pharmacy"; }
    }

    class EnrollBridgeHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(500, 1000);
            hc.setBridgeActive(true);
            return new ActionResultDto.StubResult("ShpsService", 850, Map.of(
                    "order_id", "SHPS-BRG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    "status", "BRIDGE_ACTIVE",
                    "max_duration_months", "6"
            ));
        }
        @Override public String getNextState(HubCase hc) { return "BRIDGE_ACTIVE"; }
        @Override public String getNextStage(HubCase hc) { return null; }
        @Override public String getNextActionKey(HubCase hc) { return null; }
        @Override public String getNextActionLabel(HubCase hc) { return null; }
    }

    class RecordPaDecisionHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            String decision = payload.getOrDefault("decision", "APPROVED").toString();
            // Find latest PA and update
            var pas = new java.util.ArrayList<>(new java.util.LinkedList<>(
                    java.util.Collections.emptyList()));
            return null;
        }
        @Override public String getNextState(HubCase hc) { return "PA_APPROVED".equals(hc.getPaStatus()) ? "FINANCIAL_COMPLETE" : "PA_DENIED"; }
        @Override public String getNextStage(HubCase hc) { return "PA_APPROVED".equals(hc.getPaStatus()) ? "FINANCIAL" : "PA"; }
        @Override public String getNextActionKey(HubCase hc) { return "PA_APPROVED".equals(hc.getPaStatus()) ? "EVALUATE_FA" : "SUBMIT_PA_APPEAL"; }
        @Override public String getNextActionLabel(HubCase hc) { return "PA_APPROVED".equals(hc.getPaStatus()) ? "Evaluate financial assistance" : "Submit PA appeal"; }
    }

    class SubmitPaAppealHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(600, 1200);
            return new ActionResultDto.StubResult("FaxService", 900, Map.of(
                    "fax_id", "FAX-" + UUID.randomUUID().toString().substring(0, 8),
                    "document", "PA Appeal Package",
                    "pages", "7",
                    "status", "SENT (mock)"
            ));
        }
        @Override public String getNextState(HubCase hc) { return "APPEAL_PENDING"; }
        @Override public String getNextStage(HubCase hc) { return "PA"; }
        @Override public String getNextActionKey(HubCase hc) { return "RECORD_PA_DECISION"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Record appeal outcome"; }
    }

    class RouteToSpHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(500, 1200);
            return new ActionResultDto.StubResult("SpRoutingService", 950, Map.of(
                    "sp_name", "CVS Specialty",
                    "sp_address", "1200 Health Pkwy, Columbus OH 43210",
                    "sp_phone", "800-555-0199",
                    "routing_basis", "In-network per eBV, patient preference"
            ));
        }
        @Override public String getNextState(HubCase hc) { return "TRIAGE_COMPLETE"; }
        @Override public String getNextStage(HubCase hc) { return "TRIAGE"; }
        @Override public String getNextActionKey(HubCase hc) { return "CONFIRM_FIRST_DISPENSE"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Confirm first dispense"; }
    }

    class ConfirmFirstDispenseHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            hc.setTherapyStartDate(java.time.LocalDate.now());
            return null;
        }
        @Override public String getNextState(HubCase hc) { return "THERAPY_ACTIVE"; }
        @Override public String getNextStage(HubCase hc) { return "ADHERENCE"; }
        @Override public String getNextActionKey(HubCase hc) { return "SEND_REFILL_REMINDER"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Send refill reminder"; }
    }

    class SendRefillReminderHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            simulateLatency(400, 800);
            return new ActionResultDto.StubResult("SmsService", 600, Map.of(
                    "sms_id", "SMS-" + UUID.randomUUID().toString().substring(0, 8),
                    "message_preview", "Hi James, your Velarix refill is due in 7 days. Please contact CVS Specialty at 800-555-0199.",
                    "status", "SENT (mock)"
            ));
        }
        @Override public String getNextState(HubCase hc) { return "ADHERENCE_MONITORING"; }
        @Override public String getNextStage(HubCase hc) { return "ADHERENCE"; }
        @Override public String getNextActionKey(HubCase hc) { return "LOG_CHECKIN_CALL"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Log check-in call"; }
    }

    class LogCheckinCallHandler implements ActionHandler {
        @Override
        public ActionResultDto.StubResult execute(HubCase hc, Map<String, Object> payload, AuthenticatedUser user) {
            String notes = payload.getOrDefault("notes", "Routine 30-day check-in call").toString();
            interactionRepo.save(Interaction.builder()
                    .caseId(hc.getId())
                    .interactionType("CALL")
                    .direction("OUTBOUND")
                    .channel("PHONE")
                    .contactName("Patient")
                    .contactRole("PATIENT")
                    .subject("Check-in call")
                    .body(notes)
                    .performedBy(user.id())
                    .build());
            return null;
        }
        @Override public String getNextState(HubCase hc) { return null; }
        @Override public String getNextStage(HubCase hc) { return null; }
        @Override public String getNextActionKey(HubCase hc) { return "SEND_REFILL_REMINDER"; }
        @Override public String getNextActionLabel(HubCase hc) { return "Send refill reminder"; }
    }
}
