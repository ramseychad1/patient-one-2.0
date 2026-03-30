import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CaseListItem } from '../../../core/models/case.model';
import { StatusPillComponent } from '../status-pill/status-pill.component';

@Component({
  selector: 'app-case-row',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusPillComponent],
  template: `
    <a [routerLink]="['/cases', item.id]" class="case-row">
      <span class="case-number">{{ item.caseNumber }}</span>
      <span class="patient">{{ item.patientName }}</span>
      <app-status-pill [label]="item.stage" [variant]="item.slaBreachFlag ? 'danger' : 'info'"></app-status-pill>
    </a>
  `,
  styles: [`
    .case-row { display: flex; gap: 1rem; align-items: center; padding: 0.75rem 1rem; border-bottom: var(--stroke-weight) solid var(--border-color); text-decoration: none; color: inherit; }
    .case-row:hover { background: rgba(0,0,0,0.02); }
    .case-number { font-weight: 600; min-width: 140px; }
    .patient { flex: 1; }
  `]
})
export class CaseRowComponent {
  @Input() item!: CaseListItem;
}
