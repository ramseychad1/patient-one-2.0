export interface CaseListItem {
  id: string;
  caseNumber: string;
  patientName: string;
  programName: string;
  stage: string;
  workflowState: string;
  insuranceType: string | null;
  slaBreachFlag: boolean;
  escalationFlag: boolean;
  assignedCmName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CaseDetail extends CaseListItem {
  sourceChannel: string;
  paRequired: boolean | null;
  paStatus: string | null;
  copayEligible: boolean | null;
  papEligible: boolean | null;
  papStatus: string | null;
  bridgeActive: boolean;
  quickStartActive: boolean;
  escalationReason: string | null;
  patient: Patient;
  prescriber: Prescriber | null;
  insurancePlan: InsurancePlan | null;
  benefitsVerification: BenefitsVerification | null;
  workflowStateDetail: WorkflowState | null;
  openTasks: CaseTask[];
  recentTimeline: TimelineEntry[];
  priorAuthorizations: PriorAuthorization[];
  financialAssistance: FinancialAssistance[];
}

export interface Patient {
  id: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string | null;
  addressLine1: string | null;
  city: string | null;
  state: string | null;
  zip: string | null;
  phonePrimary: string | null;
  email: string | null;
  preferredContactMethod: string | null;
  preferredLanguage: string | null;
}

export interface Prescriber {
  id: string;
  npi: string;
  firstName: string;
  lastName: string;
  practiceName: string | null;
  specialty: string | null;
  phone: string | null;
  fax: string | null;
}

export interface InsurancePlan {
  id: string;
  insuranceType: string;
  planName: string | null;
  payerName: string | null;
  memberId: string | null;
  groupNumber: string | null;
  deductibleIndividual: number | null;
  deductibleMet: number | null;
  oopMaxIndividual: number | null;
  oopMet: number | null;
  copayDrug: number | null;
  formularyTier: number | null;
  paRequired: boolean | null;
  coveredSpecialtyPharmacies: string[] | null;
  isPrimary: boolean;
}

export interface BenefitsVerification {
  id: string;
  verificationType: string;
  status: string;
  createdAt: string;
  completedAt: string | null;
}

export interface WorkflowState {
  state: string;
  nextRequiredAction: string | null;
  nextActionLabel: string | null;
  nextActionDeadline: string | null;
  enteredAt: string;
}

export interface CaseTask {
  id: string;
  caseId: string;
  taskType: string;
  title: string;
  description: string | null;
  status: string;
  priority: string;
  actionKey: string | null;
  slaBreached: boolean;
  dueAt: string | null;
  createdAt: string;
}

export interface TimelineEntry {
  id: string;
  entryType: string;
  title: string;
  description: string | null;
  performedBy: string;
  occurredAt: string;
  metadata: Record<string, string>;
}

export interface PriorAuthorization {
  id: string;
  attemptNumber: number;
  paType: string;
  status: string;
  payerName: string | null;
  submittedAt: string | null;
  determinedAt: string | null;
  authorizationNumber: string | null;
  denialReason: string | null;
  appealDeadline: string | null;
}

export interface FinancialAssistance {
  id: string;
  faType: string;
  status: string;
  effectiveDate: string | null;
  expirationDate: string | null;
  copayMaxBenefitUsd: number | null;
  papFplPercentage: number | null;
  denialReason: string | null;
}

export interface ActionResult {
  actionKey: string;
  success: boolean;
  message: string;
  stubResult: { serviceName: string; latencyMs: number; fields: Record<string, string> } | null;
  nextActionKey: string | null;
  nextActionLabel: string | null;
  caseUpdated: CaseDetail;
}

export interface Dashboard {
  myCases: CaseListItem[];
  slaBreaches: CaseListItem[];
  openTasks: CaseTask[];
  stats: {
    totalOpen: number;
    slaBreached: number;
    pendingConsent: number;
    pendingPA: number;
    pendingFA: number;
  };
}

export interface ApiResponse<T> {
  data: T;
  meta: { timestamp: string; requestId: string };
  error?: { code: string; message: string; field?: string };
}
