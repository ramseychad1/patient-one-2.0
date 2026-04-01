import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

const STAGES = ['INTAKE', 'CONSENT', 'BI_BV', 'PA', 'FINANCIAL', 'TRIAGE', 'ADHERENCE', 'CLOSED'];
const STAGE_LABELS: Record<string, string> = {
  INTAKE: 'Intake', CONSENT: 'Consent', BI_BV: 'Benefit Investigation', PA: 'PA',
  FINANCIAL: 'Financial', TRIAGE: 'Triage', ADHERENCE: 'Adherence', CLOSED: 'Closed'
};

@Component({
  selector: 'app-journey-progress',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="journey">
      @for (stage of stages; track stage) {
        <div class="step" [class.active]="stage === currentStage" [class.complete]="isComplete(stage)">
          <div class="dot"></div>
          <span class="label">{{ getLabel(stage) }}</span>
        </div>
      }
    </div>
  `,
  styles: [`
    .journey { display: flex; gap: 4px; align-items: center; }
    .step { display: flex; flex-direction: column; align-items: center; gap: 4px; flex: 1; }
    .dot { width: 12px; height: 12px; border-radius: 50%; background: var(--warm-gray-7); }
    .step.active .dot { background: var(--primary-red); }
    .step.complete .dot { background: var(--success-green); }
    .label { font-size: 0.7rem; color: var(--warm-gray-7); }
    .step.active .label { color: var(--primary-red); font-weight: 600; }
  `]
})
export class JourneyProgressComponent {
  @Input() currentStage = '';
  stages = STAGES;

  getLabel(stage: string): string { return STAGE_LABELS[stage] || stage; }
  isComplete(stage: string): boolean {
    return STAGES.indexOf(stage) < STAGES.indexOf(this.currentStage);
  }
}
