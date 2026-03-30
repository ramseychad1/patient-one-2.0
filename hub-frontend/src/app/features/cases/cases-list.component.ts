import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { CaseListItem } from '../../core/models/case.model';

@Component({
  selector: 'app-cases-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-header">
      <div class="page-title">My Cases</div>
      <div class="page-sub">{{ filteredCases().length }} cases</div>
    </div>
    <div class="page-body">
      <div class="filter-row">
        <div class="filter-chip" [class.active]="activeFilter() === 'all'" (click)="setFilter('all')">All</div>
        <div class="filter-chip" [class.active]="activeFilter() === 'breach'" (click)="setFilter('breach')">SLA breach</div>
        <div class="filter-chip" [class.active]="activeFilter() === 'intake'" (click)="setFilter('intake')">Intake</div>
        <div class="filter-chip" [class.active]="activeFilter() === 'consent'" (click)="setFilter('consent')">Consent</div>
        <div class="filter-chip" [class.active]="activeFilter() === 'pa'" (click)="setFilter('pa')">PA</div>
        <div class="filter-chip" [class.active]="activeFilter() === 'adherence'" (click)="setFilter('adherence')">Adherence</div>
      </div>

      @if (loading()) {
        <div class="loading">Loading cases...</div>
      } @else if (filteredCases().length === 0) {
        <div class="empty">No cases match this filter.</div>
      } @else {
        <div class="case-list">
          @for (c of filteredCases(); track c.id; let i = $index) {
            <div class="case-row" [class.sla-breach]="c.slaBreachFlag" (click)="openCase(c)">
              <div class="case-avatar" [ngClass]="getAvatarClass(i)">{{ getInitials(c.patientName) }}</div>
              <div class="case-body">
                <div class="case-name">{{ c.patientName }}</div>
                <div class="case-meta">{{ c.caseNumber }} · {{ c.programName }}{{ c.insuranceType ? ' · ' + c.insuranceType : '' }}</div>
              </div>
              <div class="case-right">
                <span class="stage-pill" [ngClass]="getStagePillClass(c.stage)">{{ getStageLabel(c.stage) }}</span>
                @if (c.slaBreachFlag) {
                  <span class="sla-label breach">SLA breached</span>
                } @else {
                  <span class="sla-label">{{ c.workflowState }}</span>
                }
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    :host { display: flex; flex-direction: column; flex: 1; overflow-y: auto; }
    .page-header { padding: 24px 28px 16px; background: var(--surface-card); border-bottom: 1px solid var(--border); }
    .page-title { font-size: 20px; font-weight: 700; letter-spacing: -0.03em; }
    .page-sub { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }
    .page-body { padding: 20px 24px; }
    .filter-row { display: flex; gap: 6px; margin-bottom: 16px; }
    .filter-chip { padding: 4px 10px; border-radius: var(--radius-pill); font-size: 11.5px; font-weight: 500; border: 1px solid var(--border-strong); background: var(--surface-card); color: var(--text-secondary); cursor: pointer; transition: all 0.12s; }
    .filter-chip:hover { border-color: var(--teal); color: var(--teal); }
    .filter-chip.active { background: var(--teal); border-color: var(--teal); color: #fff; }
    .loading, .empty { padding: 3rem; text-align: center; color: var(--text-secondary); }
    .case-list { background: var(--surface-card); border: 1px solid var(--border); border-radius: var(--radius-lg); overflow: hidden; }
    .case-row { display: flex; align-items: center; gap: 12px; padding: 11px 16px; border-bottom: 1px solid var(--border); cursor: pointer; transition: background 0.1s; }
    .case-row:last-child { border-bottom: none; }
    .case-row:hover { background: var(--surface-section); }
    .case-row.sla-breach { border-left: 3px solid var(--danger); }
    .case-avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0; }
    .c-purple { background: var(--purple-light); color: var(--purple); }
    .c-teal { background: var(--teal-light); color: var(--teal); }
    .c-blue { background: var(--blue-light); color: var(--blue); }
    .c-amber { background: var(--amber-light); color: var(--amber); }
    .case-body { flex: 1; min-width: 0; }
    .case-name { font-size: 13px; font-weight: 600; }
    .case-meta { font-size: 11.5px; color: var(--text-tertiary); margin-top: 1px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .case-right { display: flex; flex-direction: column; align-items: flex-end; gap: 3px; flex-shrink: 0; }
    .stage-pill { font-size: 10.5px; font-weight: 600; padding: 2px 8px; border-radius: var(--radius-pill); }
    .stage-pill.intake { background: var(--blue-light); color: var(--blue); }
    .stage-pill.consent { background: var(--amber-light); color: var(--amber); }
    .stage-pill.bi { background: var(--purple-light); color: var(--purple); }
    .stage-pill.pa { background: var(--brand-bg); color: var(--brand); }
    .stage-pill.financial { background: var(--teal-light); color: var(--teal); }
    .stage-pill.triage { background: var(--surface-page); color: var(--text-secondary); }
    .stage-pill.adherence { background: var(--teal-light); color: var(--teal-mid); }
    .sla-label { font-size: 10.5px; color: var(--text-tertiary); }
    .sla-label.breach { color: var(--danger); font-weight: 600; }
  `]
})
export class CasesListComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);

  cases = signal<CaseListItem[]>([]);
  loading = signal(true);
  activeFilter = signal('all');

  filteredCases = computed(() => {
    const all = this.cases();
    const f = this.activeFilter();
    switch (f) {
      case 'breach': return all.filter(c => c.slaBreachFlag);
      case 'intake': return all.filter(c => c.stage === 'INTAKE');
      case 'consent': return all.filter(c => c.stage === 'CONSENT');
      case 'pa': return all.filter(c => c.stage === 'PA');
      case 'adherence': return all.filter(c => c.stage === 'ADHERENCE');
      default: return all;
    }
  });

  ngOnInit() {
    this.api.getCases().subscribe({
      next: (data) => { this.cases.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  setFilter(f: string) { this.activeFilter.set(f); }
  openCase(c: CaseListItem) { this.router.navigate(['/cases', c.id]); }

  getInitials(name: string): string {
    const parts = name.split(/[, ]+/).filter(p => p.length > 0);
    if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    return name.substring(0, 2).toUpperCase();
  }

  getAvatarClass(i: number): string {
    return ['c-purple', 'c-amber', 'c-teal', 'c-blue'][i % 4];
  }

  getStagePillClass(stage: string): string {
    const map: Record<string, string> = { INTAKE: 'intake', CONSENT: 'consent', BI_BV: 'bi', PA: 'pa', FINANCIAL: 'financial', TRIAGE: 'triage', ADHERENCE: 'adherence' };
    return map[stage] || 'triage';
  }

  getStageLabel(stage: string): string {
    const map: Record<string, string> = { INTAKE: 'Intake', CONSENT: 'Consent', BI_BV: 'BI / BV', PA: 'Prior Auth', FINANCIAL: 'Financial', TRIAGE: 'SP Triage', ADHERENCE: 'Adherence', CLOSED: 'Closed' };
    return map[stage] || stage;
  }
}
