# CLAUDE.md — HubAccess v2
## Complete Briefing for Claude Code
_Read this entire file before writing a single line of code._
_Version 2.0 — Greenfield. Supersedes all previous CLAUDE.md content._

---

## 0. Non-Negotiable Rules (Read First)

These rules are absolute. Any deviation is a defect, not a style choice.

**Rule 1 — Mockups are law.**
Every UI screen has a corresponding mockup HTML file in `/mockups/`.
Before building any Angular component, open the mockup. Match it exactly.
Pixel-level fidelity is required. Do not improvise layout, colors, spacing,
or component structure. If the mockup shows a button labeled "Run BI/BV",
the Angular button says "Run BI/BV" — not "Run Benefits Verification",
not "Verify Benefits". Exact match.

**Rule 2 — Actions drive state, not PUTs.**
The backend API is action-oriented. CMs invoke actions; the server determines
resulting state. There is no `PUT /cases/{id}/status`. There is
`POST /cases/{id}/actions/run-ebv`. Every CM action follows this pattern.
See Section 7 for the full action inventory.

**Rule 3 — The AKS rule is triple-enforced.**
Government-insured patients (Medicare, Medicaid, TRICARE, VA, Medicare Advantage)
cannot receive copay assistance. This is enforced at:
(a) UI layer — copay action not rendered
(b) Service layer — `AksViolationException` thrown
(c) Database layer — RLS policy rejects INSERT
Do not weaken any of these layers under any circumstances.

**Rule 4 — The consent gate is hard.**
`RUN_EBV` action throws `PrerequisiteNotMetException` if `consent_hipaa = false`
or `consent_program = false` on the case's `EnrollmentRecord`.
The UI disables the BI/BV action button with a tooltip explaining the gate.

**Rule 5 — Every stub call is logged.**
Every call to any stub service MUST create a `ServiceCallLog` record.
No exceptions. This is how the demo shows "what just happened" in the
Stub Result Panel on the case detail screen.

**Rule 6 — State is append-only audit.**
`CaseStatusHistory` rows are never updated or deleted.
`Interaction` rows are never updated or deleted.
Corrections create new records. This is HIPAA.

**Rule 7 — PHI stays out of logs.**
The `ServiceCallLog` `request_payload` and `response_payload` fields
must have PHI redacted before storage. Patient name, DOB, address, phone,
member ID, and income data are redacted with `[REDACTED]`.

---

## 1. Project Overview

HubAccess is a purpose-built, action-oriented case management platform for
hub services organizations supporting specialty pharmaceutical manufacturers.

It manages the end-to-end patient journey — from enrollment through ongoing
therapy adherence — for Case Managers who guide patients through insurance
verification, prior authorization, financial assistance, and specialty pharmacy
triage.

**What makes this different from a CRM:**
Every screen answers the same question: *"What does the CM need to do right now?"*
The system surfaces the next required action, enables it, executes it (calling
a stub service), logs the result, and advances the case state. The CM never
has to wonder what comes next.

**Reference documents in this repository:**
- `hub-requirements-v2.md` — Complete business requirements
- `hub-data-model-v2.md` — All 22 entities with field-level detail
- `hub-tech-stack-v2.md` — Stack, API design, stub pattern, UI architecture
- `/mockups/` — 12 HTML mockup files (pixel-level implementation guides)

---

## 2. Repository Structure

```
hub-platform/
  hub-api/                   # Spring Boot 3.3, Java 21
    src/main/java/com/hubaccess/
    src/main/resources/
      application.yml
      db/migration/          # Flyway migrations
    src/test/
    Dockerfile
    pom.xml
  hub-frontend/              # Angular 18
    src/app/
      core/
        auth/
        layout/
      features/
        enrollment/          # S-03 + S-04
        cases/               # S-05
        consent-mi/          # S-06
        bi-bv/               # S-07
        pa-appeals/          # S-08
        financial/           # S-09
        sp-triage/           # S-10
        adherence/           # S-11
        admin/               # S-12
      shared/
        components/
          journey-stepper/
          action-panel/
          case-header/
          timeline/
          sla-badge/
          stub-result-panel/
        services/
        models/
    Dockerfile
    nginx.conf
    package.json
  mockups/                   # 12 HTML mockup files
  docker-compose.yml
  .env.example
  README.md
  CLAUDE.md                  # This file
```

---

## 3. Tech Stack — Exact Versions

### Backend
| Concern | Choice |
|---------|--------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.x |
| Web | Spring Web MVC (not WebFlux) |
| Security | Spring Security 6 + JWT (jjwt 0.12.x) |
| ORM | Spring Data JPA + Hibernate 6 |
| Migrations | Flyway |
| Validation | Jakarta Bean Validation |
| API docs | SpringDoc OpenAPI 3 |
| Build | Maven |
| Boilerplate | Lombok |
| DTO mapping | MapStruct |
| Testing | JUnit 5, Mockito, Spring Boot Test, Testcontainers |
| Virtual threads | `spring.threads.virtual.enabled=true` |

### Frontend
| Concern | Choice |
|---------|--------|
| Framework | Angular 18 |
| Language | TypeScript 5 |
| UI components | Angular Material 18 |
| State | NgRx Signals |
| HTTP | Angular HttpClient + JWT interceptor |
| Forms | Reactive Forms |
| Date | date-fns |
| Charts | Chart.js via ng2-charts |

### Database
| Concern | Choice |
|---------|--------|
| Engine | PostgreSQL 15 |
| Hosting | Supabase |
| RLS | Enforced from day one |
| Pooling | Supabase PgBouncer + HikariCP |

### Infrastructure
| Concern | Choice |
|---------|--------|
| Containerization | Docker + Docker Compose (local) |
| Deployment | Railway (two services: hub-api, hub-frontend) |
| CI/CD | GitHub Actions (deploy on merge to main) |
| SMS (MVP) | MockSmsService — interface defined, Twilio-ready |
| Background jobs | Spring @Scheduled (in-process) |

---

## 4. Data Model Summary

22 entities across 7 layers. Full detail in `hub-data-model-v2.md`.

| Layer | Entities |
|-------|---------|
| 1 · Tenant | Manufacturer, Program, ProgramConfig |
| 2 · Case Core | Patient, Prescriber, HubCase |
| 3 · Insurance & Enrollment | InsurancePlan, BenefitsVerification, EnrollmentRecord |
| 4 · Workflow (NEW) | WorkflowState |
| 5 · Financial, PA, Outreach | FinancialAssistanceCase, PriorAuthorization, PatientOutreach |
| 6 · Activity & Audit | CaseTask, Interaction, CaseStatusHistory, ServiceCallLog (NEW) |
| 7 · Security | HubUser, Role, UserRole, UserProgramAssignment |

**Key new entities in v2:**
- `WorkflowState` — drives the action panel; one record per state per case
- `ServiceCallLog` — logs every stub call; surfaces in demo stub result panel

---

## 5. Security Architecture

### RBAC (Spring Security)
```java
// Method-level security on every service method
@PreAuthorize("hasRole('CASE_MANAGER') and @programAccessChecker.canAccess(#caseId)")
public WorkflowTransitionResult performAction(...) { ... }
```

### Row-Level Security (PostgreSQL)
```sql
-- Every case-related table has this policy
CREATE POLICY cm_case_isolation ON hub_case
  USING (
    program_id IN (
      SELECT program_id FROM user_program_assignment
      WHERE user_id = current_setting('app.current_user_id')::uuid
      AND (expires_at IS NULL OR expires_at > NOW())
    )
    OR EXISTS (
      SELECT 1 FROM hub_user
      WHERE id = current_setting('app.current_user_id')::uuid
      AND is_hub_admin = true
    )
  );
```

The application sets `app.current_user_id` on every database connection
using a Hibernate interceptor. Both RBAC and RLS are enforced independently —
defense-in-depth.

---

## 6. Stub Architecture

All external integrations are stubbed. Every stub:
1. Implements a Java interface
2. Is annotated `@Profile("!production")`
3. Reads `DEMO_SCENARIO` env var to vary responses
4. Simulates realistic latency (500–2000ms)
5. Creates a `ServiceCallLog` record on every call

```java
public interface EbvService { EbvResult runEbv(EbvRequest req); }
public interface SmsService  { SmsResult send(SmsRequest req); }
public interface FaxService  { FaxResult send(FaxRequest req); }
public interface EpaService  { EpaResult submitPa(EpaRequest req); }
public interface EivService  { EivResult verify(EivRequest req); }
public interface OcrService  { OcrResult extract(byte[] pdf); }
public interface CopayService { CopayResult enroll(CopayRequest req); }
public interface ShpsService  { ShpsResult dispense(ShpsRequest req); }
public interface SpRoutingService { SpResult route(SpRoutingRequest req); }
```

Production swap: add `@Profile("production")` implementation, no other changes.

### Demo Scenarios

Set `DEMO_SCENARIO` in environment to control stub responses:

| Value | Patient type | PA outcome | PAP outcome |
|-------|-------------|------------|-------------|
| `DEFAULT` | Commercial, BlueCross PPO | Approved | N/A |
| `GOVERNMENT_PATIENT` | Medicare | N/A | Eligible (FPL 280%) |
| `UNINSURED_PATIENT` | Uninsured | N/A | Eligible (FPL 195%) |
| `PA_DENIED` | Commercial | Denied → appeal | N/A |
| `EIV_INELIGIBLE` | Uninsured | N/A | Ineligible (FPL 520%) |
| `SHPS_DELAYED` | PAP | N/A | Approved, ship delayed |

---

## 7. Action Inventory (Complete)

Every CM action has a corresponding:
- API endpoint: `POST /api/v1/cases/{id}/actions/{action-key}`
- `ActionValidator` implementation
- `ActionExecutor` implementation (calls stub if external service involved)
- `WorkflowState` transition entry
- `CaseTask` auto-creation for next step
- UI: action button in `ActionPanelComponent`

### Enrollment Actions (create case)
| Action | Endpoint | Stub |
|--------|---------|------|
| Submit fax/PDF enrollment | `POST /api/v1/cases` (source=FAX_PDF) | OcrService |
| Submit eRX enrollment | `POST /api/v1/cases` (source=ERX) | ErxService |
| Submit DEP enrollment | `POST /api/v1/cases` (source=DEP) | none |
| Send MI request | `.../send-mi-request` | SmsService |
| Resolve MI | `.../resolve-mi` | none |

### Consent Actions
| Action | Endpoint | Stub |
|--------|---------|------|
| Send consent request | `.../send-consent-request` | SmsService |
| Confirm consent | `.../confirm-consent` | none |

### BI/BV Actions
| Action | Endpoint | Stub |
|--------|---------|------|
| Run eBV | `.../run-ebv` | EbvService |
| Start manual BI | `.../start-manual-bi` | none |
| Complete manual BI | `.../complete-manual-bi` | none |

### PA Actions
| Action | Endpoint | Stub |
|--------|---------|------|
| Generate PA package | `.../generate-pa-package` | FaxService |
| Record PA submission | `.../record-pa-submission` | none |
| Check PA status | `.../check-pa-status` | EpaService |
| Record PA outcome | `.../record-pa-outcome` | none |
| Initiate appeal | `.../initiate-appeal` | FaxService |
| Record appeal outcome | `.../record-appeal-outcome` | none |
| Initiate Quick Start | `.../initiate-quick-start` | ShpsService |
| Initiate Bridge | `.../initiate-bridge` | ShpsService |

### Financial Actions
| Action | Endpoint | Stub |
|--------|---------|------|
| Assess copay eligibility | `.../assess-copay` | none (rule-based) |
| Enroll in copay | `.../enroll-copay` | CopayService |
| Run PAP pre-screen | `.../run-pap-prescreen` | none |
| Run eIV | `.../run-eiv` | EivService |
| Assess PAP eligibility | `.../assess-pap` | none (FPL calc) |
| Approve PAP | `.../approve-pap` | ShpsService |
| Deny PAP | `.../deny-pap` | none |

### Triage Actions
| Action | Endpoint | Stub |
|--------|---------|------|
| Triage to SP | `.../triage-to-sp` | SpRoutingService + FaxService |
| Confirm first dispense | `.../confirm-first-dispense` | none |
| Send tracking to patient | `.../send-tracking` | SmsService |

### Adherence Actions
| Action | Endpoint | Stub |
|--------|---------|------|
| Send refill reminder | `.../send-refill-reminder` | SmsService |
| Log check-in | `.../log-checkin` | none |
| Log adverse event | `.../log-adverse-event` | none |

### Case Management Actions
| Action | Endpoint | Stub |
|--------|---------|------|
| Add note | `POST /cases/{id}/interactions` | none |
| Escalate case | `.../escalate` | none |
| Reassign CM | `.../reassign` | none |
| Close case | `.../close-case` | none |

---

## 8. Mockup Reference — Mandatory Pre-Build Step

Before building any Angular feature module, open the corresponding mockup file
in a browser. Study it. Then build to match it.

| Feature module | Mockup file | Screens covered |
|---------------|-------------|-----------------|
| enrollment | mockup-enroll-launcher.html | S-03 |
| enrollment | mockup-enroll-modals.html | S-04 |
| cases | mockup-case-detail.html | S-05 |
| consent-mi | mockup-consent-mi.html | S-06 |
| bi-bv | mockup-bi-bv.html | S-07 |
| pa-appeals | mockup-pa-appeals.html | S-08 |
| financial | mockup-financial.html | S-09 |
| sp-triage | mockup-sp-triage.html | S-10 |
| adherence | mockup-adherence.html | S-11 |
| admin | mockup-admin.html | S-12 |

### Mockup-to-Component Mapping (mandatory)

| Mockup element | Angular component | Notes |
|---------------|-------------------|-------|
| Progress bar at top of case | `JourneyStepperComponent` | Always visible on case screens |
| Primary action button | `ActionPanelComponent` | Reads `WorkflowState.nextActionLabel` |
| Stub result box | `StubResultPanelComponent` | Shown after any stub-calling action |
| Patient name / case # bar | `CaseHeaderComponent` | All case screens |
| SLA timer badge | `SlaBadgeComponent` | On tasks + action panel |
| Interaction list | `TimelineComponent` | Bottom of case detail |
| 3 entry point tiles | `EnrollmentLauncherComponent` | S-03 |
| Modal forms | `FaxEnrollModalComponent`, `ErxEnrollModalComponent`, `DepEnrollModalComponent` | S-04 |

**Do not build a component that is not in this table or in a mockup.**
**Do not build a component that differs from its mockup counterpart.**

---

## 9. Seed Data

The following seed data must be created via Flyway (or a `DataInitializer` bean)
on first startup. This enables demo without manual setup.

### Seed Manufacturer
```sql
INSERT INTO manufacturer (id, name, status, created_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'Meridian Therapeutics', 'ACTIVE', NOW());
```

### Seed Program
```sql
INSERT INTO program (id, manufacturer_id, name, drug_brand_name, 
                     drug_generic_name, therapeutic_area, status, created_at)
VALUES (
  '22222222-2222-2222-2222-222222222222',
  '11111111-1111-1111-1111-111111111111',
  'Meridian Access Program', 'Velarix', 'velarixumab',
  'Inflammatory Disease', 'ACTIVE', NOW()
);
```

### Seed ProgramConfig (all features enabled for demo)
```sql
INSERT INTO program_config (id, program_id, prior_auth_required,
  copay_assistance_enabled, pap_enabled, bridge_supply_enabled,
  quick_start_enabled, adherence_program_enabled, ebv_enabled, eiv_enabled,
  fpl_threshold_pct, pap_approval_duration_months, pa_submit_sla_business_days,
  pa_appeal_window_days)
VALUES (
  '33333333-3333-3333-3333-333333333333',
  '22222222-2222-2222-2222-222222222222',
  true, true, true, true, true, true, true, true,
  400, 12, 3, 30
);
```

### Seed Users (demo login credentials)
```
CM: sarah.chen@hubaccess.demo / Demo1234!
Admin: admin@hubaccess.demo / Demo1234!
```

```sql
-- Passwords stored as bcrypt hash
INSERT INTO hub_user (id, email, password_hash, first_name, last_name, is_active, created_at)
VALUES 
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 
   'sarah.chen@hubaccess.demo', '$2a$10$[BCRYPT_HASH]', 
   'Sarah', 'Chen', true, NOW()),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
   'admin@hubaccess.demo', '$2a$10$[BCRYPT_HASH]',
   'Hub', 'Admin', true, NOW());

INSERT INTO role (id, name) VALUES
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'CASE_MANAGER'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'HUB_ADMIN');

INSERT INTO user_role (id, user_id, role_id, assigned_at) VALUES
  (gen_random_uuid(), 'aaaaaaaa-...', 'cccccccc-...', NOW()),
  (gen_random_uuid(), 'bbbbbbbb-...', 'dddddddd-...', NOW());

INSERT INTO user_program_assignment (id, user_id, program_id, 
  can_create_cases, can_edit_cases, can_view_financials, can_perform_actions, assigned_at)
VALUES
  (gen_random_uuid(), 'aaaaaaaa-...', '22222222-...', true, true, true, true, NOW());
```

### Seed Test Patient (pre-built demo case)

A pre-built case in the `BI_PENDING` state allows demo to jump directly to the
most interesting action (Run BI/BV) without walking through intake.

```sql
-- Patient: James Rodriguez, DOB 1978-03-12
-- Pre-built at CONSENT_CONFIRMED → ready for BI
-- See full seed SQL in hub-api/src/main/resources/db/seed/demo-case.sql
```

---

## 10. Build Order

Build in this exact sequence. Each phase must be fully working before the next begins.

### Phase 1 — Foundation (backend)
1. Project scaffold: Spring Boot 3.3, Maven, dependencies
2. Flyway migrations: all 22 entities (from `hub-data-model-v2.md`)
3. JPA entities + repositories: all 22
4. JWT auth: login endpoint, token generation, Spring Security config
5. RLS wiring: Hibernate interceptor sets `app.current_user_id`
6. Seed data: manufacturer, program, config, users, demo case
7. Health check: `GET /actuator/health` returns UP with DB connection

### Phase 2 — Stub Services
8. All 9 stub service interfaces + mock implementations
9. `ServiceCallLog` entity + repository + logging in every stub
10. `DEMO_SCENARIO` env var wired into all stubs
11. Unit tests: each stub returns expected data for each scenario

### Phase 3 — Workflow Engine
12. `WorkflowState` repository + `CaseStatusHistory` repository
13. `ActionValidator` interface + all validators
14. `ActionExecutor` interface + all executors
15. `CaseWorkflowService.transition()` — the state machine core
16. `NextActionResolver` — determines next action from state
17. `CaseTask` auto-creation on state transition
18. All action endpoints: `POST /cases/{id}/actions/{action-key}`
19. Integration tests: full workflow walk-through (enrollment → closed)

### Phase 4 — Read Endpoints
20. `GET /cases` — CM's case list, filtered, paginated
21. `GET /cases/{id}` — case detail with current workflow state
22. `GET /cases/{id}/tasks` — open tasks
23. `GET /cases/{id}/interactions` — timeline
24. `GET /dashboard` — CM home data

### Phase 5 — Angular Foundation
25. Project scaffold: Angular 18, Material, NgRx Signals
26. Core: auth service, JWT interceptor, login guard
27. Shell: app layout, navigation (sidebar), header
28. Shared components: JourneyStepperComponent, ActionPanelComponent,
    CaseHeaderComponent, TimelineComponent, SlaBadgeComponent, StubResultPanelComponent

### Phase 6 — Screen by Screen (follow mockups exactly)
Build in this order — earlier screens are simpler and build confidence:

29. S-01: Login screen → mockup-login.html
30. S-02: CM Home → mockup-cm-home.html
31. S-03 + S-04: Enrollment launcher + modals → mockup-enroll-launcher.html, mockup-enroll-modals.html
32. S-05: Case detail (action engine) → mockup-case-detail.html
33. S-06: Consent + MI → mockup-consent-mi.html
34. S-07: Benefits investigation → mockup-bi-bv.html
35. S-08: PA + appeals → mockup-pa-appeals.html
36. S-09: Financial assistance → mockup-financial.html
37. S-10: SP triage → mockup-sp-triage.html
38. S-11: Ongoing adherence → mockup-adherence.html
39. S-12: Hub admin → mockup-admin.html

### Phase 7 — Scheduler + SLA
40. SLA monitoring scheduler (30-minute check)
41. Preemptive reminder scheduler (daily 6am)
42. Refill reminder scheduler (daily 7am)
43. Escalation flag propagation

### Phase 8 — Polish + Demo Readiness
44. DEMO_SCENARIO switcher: admin UI to change scenario without restart
45. Stub result panel: real-time display on every action
46. Error handling: all error states have graceful UI
47. Loading states: all async operations show progress
48. End-to-end demo walk-through: default scenario, PA_DENIED scenario, GOVERNMENT_PATIENT scenario

---

## 11. Environment Variables

```bash
# hub-api/.env.example

# Database (Supabase)
SPRING_DATASOURCE_URL=jdbc:postgresql://[supabase-host]:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=[supabase-password]

# JWT
JWT_SECRET=[min-32-char-random-string]
JWT_EXPIRATION_MS=86400000

# App
SPRING_PROFILES_ACTIVE=default
DEMO_SCENARIO=DEFAULT

# Supabase (for RLS setup via MCP)
SUPABASE_URL=https://[project].supabase.co
SUPABASE_ANON_KEY=[anon-key]
SUPABASE_SERVICE_KEY=[service-key]
```

---

## 12. Definition of Done (per feature)

A feature is done when ALL of the following are true:

- [ ] Matches mockup exactly (open mockup in browser, compare side-by-side)
- [ ] Action calls stub, stub logs to ServiceCallLog
- [ ] WorkflowState updates correctly after action
- [ ] Next action panel shows correct next action
- [ ] CaseStatusHistory record created
- [ ] SLA timer shown where applicable
- [ ] Stub result panel shows stub response after action
- [ ] Error state handled gracefully (not blank screen / 500 shown to user)
- [ ] RBAC enforced (action fails with 403 if CM not assigned to program)
- [ ] RLS enforced (case not returned in list if CM not assigned)
- [ ] Unit test covers happy path
- [ ] Unit test covers error/rejection path
- [ ] Console has no errors

---

## 13. What NOT to Build

Do not build any of the following. They are out of scope for MVP.

- Supervisor Kanban pipeline view
- Manufacturer client dashboard / outcomes reporting
- Patient self-service portal (separate product)
- HCP portal (separate product)
- Real SureScripts integration (stub only)
- Real payer eBV integration (stub only)
- Real Agadia ePA integration (stub only)
- Real Twilio SMS (stub only; interface is ready)
- Real Experian eIV (stub only)
- Real SHPS/GuardianRx integration (stub only)
- Camunda BPMN engine (Spring state machine is sufficient)
- Kafka event bus (Spring events is sufficient)
- Email notifications (SMS is primary channel)
- Document storage / S3 (PDF upload is mock at MVP)
- Reimbursement Coordinator specific views
- Care Coordinator specific views
- Field Solutions Coordinator specific views
- Multi-program case (one case per program per patient — by design)
- Any AI/agent features (architecture supports it, MVP does not build it)
