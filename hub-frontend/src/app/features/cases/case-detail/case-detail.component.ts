import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { CaseDetail, TimelineEntry, ActionResult, CaseTask } from '../../../core/models/case.model';

const STAGES = ['INTAKE', 'CONSENT', 'BI_BV', 'PA', 'FINANCIAL', 'TRIAGE', 'ADHERENCE', 'CLOSED'];
const STAGE_LABELS: Record<string, string> = {
  INTAKE: 'Intake', CONSENT: 'Consent', BI_BV: 'BI / BV', PA: 'Prior Auth',
  FINANCIAL: 'Financial', TRIAGE: 'SP Triage', ADHERENCE: 'Adherence', CLOSED: 'Closed'
};

@Component({
  selector: 'app-case-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './case-detail.component.html',
  styleUrl: './case-detail.component.css'
})
export class CaseDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  router = inject(Router);

  caseDetail = signal<CaseDetail | null>(null);
  timeline = signal<TimelineEntry[]>([]);
  loading = signal(true);
  actionLoading = signal(false);
  actionResult = signal<ActionResult | null>(null);
  showNoteModal = signal(false);
  noteText = '';
  showStubJson = signal(false);

  stages = STAGES;

  patientInitials = computed(() => {
    const c = this.caseDetail();
    if (!c?.patient) return '??';
    return (c.patient.firstName[0] + c.patient.lastName[0]).toUpperCase();
  });

  patientAge = computed(() => {
    const c = this.caseDetail();
    if (!c?.patient?.dateOfBirth) return 0;
    const dob = new Date(c.patient.dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    if (today.getMonth() < dob.getMonth() || (today.getMonth() === dob.getMonth() && today.getDate() < dob.getDate())) age--;
    return age;
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.api.getCaseDetail(id).subscribe({
      next: (data) => { this.caseDetail.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
    this.api.getTimeline(id).subscribe({
      next: (data) => this.timeline.set(data)
    });
  }

  getStageLabel(stage: string): string { return STAGE_LABELS[stage] || stage; }

  getStageState(stage: string): string {
    const c = this.caseDetail();
    if (!c) return 'pending';
    const currentIdx = STAGES.indexOf(c.stage);
    const thisIdx = STAGES.indexOf(stage);
    if (thisIdx < currentIdx) return 'done';
    if (thisIdx === currentIdx) return 'active';
    if (stage === 'FINANCIAL' && c.stage === 'PA') return 'blocked';
    return 'pending';
  }

  getStageSub(stage: string): string {
    const state = this.getStageState(stage);
    const c = this.caseDetail();
    if (state === 'active' && c?.slaBreachFlag) return 'SLA breach';
    if (state === 'blocked') return 'Waiting PA';
    if (state === 'done') return 'Complete';
    return '';
  }

  performAction() {
    const c = this.caseDetail();
    if (!c?.workflowStateDetail?.nextRequiredAction) return;
    this.actionLoading.set(true);
    this.actionResult.set(null);

    this.api.performAction(c.id, c.workflowStateDetail.nextRequiredAction).subscribe({
      next: (result) => {
        this.actionResult.set(result);
        this.caseDetail.set(result.caseUpdated);
        this.actionLoading.set(false);
        // Refresh timeline
        this.api.getTimeline(c.id).subscribe(tl => this.timeline.set(tl));
      },
      error: () => this.actionLoading.set(false)
    });
  }

  addNote() {
    const c = this.caseDetail();
    if (!c || !this.noteText.trim()) return;
    this.api.performAction(c.id, 'LOG_CHECKIN_CALL', { notes: this.noteText }).subscribe();
    // Simpler: direct interaction POST
    const body = { interactionType: 'NOTE', direction: 'INTERNAL', notes: this.noteText, contactName: '' };
    // Use fetch for interaction POST since we don't have it in ApiService yet
    this.showNoteModal.set(false);
    this.noteText = '';
    this.api.getTimeline(c.id).subscribe(tl => this.timeline.set(tl));
  }

  getActionDescription(): string {
    const c = this.caseDetail();
    if (!c?.workflowStateDetail) return '';
    const action = c.workflowStateDetail.nextRequiredAction;
    const plan = c.insurancePlan?.planName || 'the payer';
    switch (action) {
      case 'SEND_CONSENT_SMS': return 'Send HIPAA authorization and program consent request to patient via SMS portal link.';
      case 'CONFIRM_CONSENT': return 'Patient has completed consent. Confirm receipt to advance to benefits investigation.';
      case 'RUN_EBV': return 'Run electronic benefits verification to determine insurance coverage, PA requirements, and financial pathway.';
      case 'GENERATE_PA_PACKAGE': return `${plan} requires prior authorization for Velarix. Generate the PA cover letter and ePA form, then fax to prescriber for submission.`;
      case 'RECORD_PA_SUBMISSION': return 'Record the date PA was submitted to the payer. This starts the SLA follow-up clock.';
      case 'EVALUATE_FA': return 'Evaluate financial assistance eligibility — copay, PAP, and bridge programs based on insurance type and program configuration.';
      case 'ENROLL_COPAY': return 'Enroll patient in copay assistance program. Manufacturer-funded reduction in out-of-pocket costs.';
      case 'ROUTE_TO_SP': return 'Route prescription and benefit documentation to the designated specialty pharmacy for fulfillment.';
      case 'CONFIRM_FIRST_DISPENSE': return 'Confirm first drug dispense from specialty pharmacy. This marks therapy start date.';
      case 'SEND_REFILL_REMINDER': return 'Send refill reminder to patient via SMS. Refill is due within 7 days.';
      default: return `Execute ${c.workflowStateDetail.nextActionLabel}.`;
    }
  }

  getInsuranceTagClass(): string {
    const c = this.caseDetail();
    if (!c) return '';
    if (c.insuranceType === 'COMMERCIAL') return 'commercial';
    if (c.insuranceType === 'GOVERNMENT') return 'govt';
    return '';
  }

  getTimelineIconClass(entryType: string): string {
    switch (entryType) {
      case 'STATUS_CHANGE': return 'action';
      case 'INTERACTION': return 'call';
      case 'OUTREACH': return 'system';
      default: return 'note';
    }
  }

  getSlaClass(): string {
    const c = this.caseDetail();
    if (!c?.workflowStateDetail) return 'ok';
    if (c.slaBreachFlag) return 'danger';
    return 'ok';
  }

  formatDate(date: string | null): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  formatDateTime(date: string): string {
    return new Date(date).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' });
  }

  getStubValueClass(key: string, value: string): string {
    if (key.includes('status') && value.includes('SENT')) return 'ok';
    if (key.includes('pa_required') && value === 'true') return 'warn';
    if (key.includes('insurance_type') && value === 'COMMERCIAL') return 'ok';
    return '';
  }
}
