import { Component, Input, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';

interface BiCase {
  id: string;
  biCaseNumber: string | null;
  biType: string | null;
  status: string;
  reason: string | null;
  obcAttempt1At: string | null;
  obcAttempt1Result: string | null;
  obcAttempt2At: string | null;
  obcAttempt2Result: string | null;
  obcAttempt3At: string | null;
  obcAttempt3Result: string | null;
  smsSentAt: string | null;
  faxToHcpSentAt: string | null;
  coverageOutcome: string | null;
  planType: string | null;
  formularyTier: number | null;
  paRequired: boolean | null;
}

interface PapCase {
  id: string;
  papCaseNumber: string | null;
  status: string;
  reason: string | null;
  fplPercentage: number | null;
  householdSize: number | null;
  annualIncomeUsd: number | null;
  incomeVerifiedMethod: string | null;
  hardshipWaiverApplied: boolean | null;
  approvalEffectiveDate: string | null;
  approvalExpiryDate: string | null;
  denialReason: string | null;
  ineligibleReason: string | null;
}

interface CopayCase {
  id: string;
  copayCaseNumber: string | null;
  status: string;
  reason: string | null;
  aksCheckPassed: boolean | null;
  aksBlockReason: string | null;
  cardNumber: string | null;
  bin: string | null;
  pcn: string | null;
  groupCode: string | null;
  maxBenefitUsd: number | null;
  usedYtdUsd: number | null;
}

interface AeCase {
  id: string;
  aeCaseNumber: string | null;
  aeType: string;
  status: string;
}

interface MissingInfo {
  id: string;
  miNumber: string | null;
  category: string;
  detail: string;
  miType: string;
  reportedDate: string;
  receivedDate: string | null;
}

@Component({
  selector: 'app-child-cases',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './child-cases.component.html',
  styleUrl: './child-cases.component.css'
})
export class ChildCasesComponent implements OnInit {
  @Input() caseId!: string;
  @Input() caseNumber!: string;
  @Input() insurancePlan: any;
  @Input() priorAuthorizations: any[] = [];

  private http = inject(HttpClient);
  private baseUrl = environment.apiBaseUrl;

  activeTab = signal<'bi' | 'pa' | 'copay' | 'pap'>('bi');
  biCases = signal<BiCase[]>([]);
  papCases = signal<PapCase[]>([]);
  copayCases = signal<CopayCase[]>([]);
  aeCases = signal<AeCase[]>([]);
  missingInfo = signal<MissingInfo[]>([]);
  loading = signal(true);

  ngOnInit() {
    forkJoin({
      bi: this.http.get<any>(`${this.baseUrl}/cases/${this.caseId}/bi-cases`).pipe(map(r => r.data)),
      pap: this.http.get<any>(`${this.baseUrl}/cases/${this.caseId}/pap-cases`).pipe(map(r => r.data)),
      copay: this.http.get<any>(`${this.baseUrl}/cases/${this.caseId}/copay-cases`).pipe(map(r => r.data)),
      ae: this.http.get<any>(`${this.baseUrl}/cases/${this.caseId}/ae-cases`).pipe(map(r => r.data)),
      mi: this.http.get<any>(`${this.baseUrl}/cases/${this.caseId}/missing-info`).pipe(map(r => r.data))
    }).subscribe({
      next: (data) => {
        this.biCases.set(data.bi);
        this.papCases.set(data.pap);
        this.copayCases.set(data.copay);
        this.aeCases.set(data.ae);
        this.missingInfo.set(data.mi);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  get childCaseCount(): number {
    let count = 0;
    if (this.biCases().length > 0) count++;
    if (this.priorAuthorizations.length > 0) count++;
    if (this.copayCases().length > 0) count++;
    if (this.papCases().length > 0) count++;
    return count;
  }

  setTab(tab: 'bi' | 'pa' | 'copay' | 'pap') {
    this.activeTab.set(tab);
  }

  getStatusPillClass(status: string): string {
    switch (status) {
      case 'Completed': case 'Approved': case 'Active': return 'ok';
      case 'Pending': case 'New': case 'InProgress': return 'blue';
      case 'Denied': case 'Cancelled': return 'neutral';
      default: return 'blue';
    }
  }

  getBiTabDotColor(): string { return 'var(--ok)'; }
  getPaTabDotColor(): string {
    const pa = this.priorAuthorizations[0];
    if (!pa) return 'var(--text-tertiary)';
    if (pa.status === 'APPROVED') return 'var(--ok)';
    if (pa.status === 'DENIED') return 'var(--danger)';
    return 'var(--danger)';
  }
  getCopayTabDotColor(): string { return 'var(--amber-mid)'; }
  getPapTabDotColor(): string { return 'var(--purple)'; }

  getPaStatusPillClass(): string {
    const pa = this.priorAuthorizations[0];
    if (!pa) return 'neutral';
    if (pa.status === 'APPROVED') return 'ok';
    if (pa.status === 'DENIED') return 'danger';
    return 'danger';
  }

  formatDate(date: string | null): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  formatAttemptDate(date: string | null): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  formatCurrency(value: number | null): string {
    if (value == null) return '—';
    return '$' + value.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }
}
