import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/auth/auth.service';
import { Dashboard, CaseListItem, CaseTask } from '../../core/models/case.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private api = inject(ApiService);
  private authService = inject(AuthService);
  router = inject(Router);

  dashboard = signal<Dashboard | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  activeFilter = signal('all');
  newCaseToast = signal<{caseNumber: string; patientName: string; id: string} | null>(null);
  newCaseId = signal<string | null>(null);

  user = this.authService.user;

  greeting = computed(() => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  });

  filteredCases = computed(() => {
    const d = this.dashboard();
    if (!d) return [];
    const filter = this.activeFilter();
    switch (filter) {
      case 'breach': return d.myCases.filter(c => c.slaBreachFlag);
      case 'action': return d.myCases.filter(c => !c.slaBreachFlag && !['ADHERENCE', 'CONSENT', 'INTAKE'].includes(c.stage));
      case 'waiting': return d.myCases.filter(c => ['CONSENT', 'INTAKE'].includes(c.stage));
      case 'pa': return d.myCases.filter(c => c.stage === 'PA');
      case 'therapy': return d.myCases.filter(c => c.stage === 'ADHERENCE');
      default: return d.myCases;
    }
  });

  ngOnInit() {
    // Check for newly created case
    const stored = localStorage.getItem('ha_new_case');
    if (stored) {
      const nc = JSON.parse(stored);
      if (Date.now() - nc.timestamp < 60000) { // within last 60 seconds
        this.newCaseToast.set(nc);
        this.newCaseId.set(nc.id);
        setTimeout(() => this.newCaseToast.set(null), 8000);
      }
      localStorage.removeItem('ha_new_case');
    }

    this.api.getDashboard().subscribe({
      next: (data) => { this.dashboard.set(data); this.loading.set(false); },
      error: (err) => { this.error.set('Failed to load dashboard'); this.loading.set(false); }
    });
  }

  setFilter(filter: string) { this.activeFilter.set(filter); }

  openCase(caseItem: CaseListItem) { this.router.navigate(['/cases', caseItem.id]); }

  getInitials(name: string): string {
    const parts = name.split(/[, ]+/).filter(p => p.length > 0);
    if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    return name.substring(0, 2).toUpperCase();
  }

  getAvatarClass(index: number): string {
    const classes = ['c-purple', 'c-amber', 'c-teal', 'c-blue'];
    return classes[index % classes.length];
  }

  getStagePillClass(stage: string): string {
    const map: Record<string, string> = {
      INTAKE: 'intake', CONSENT: 'consent', BI_BV: 'bi', PA: 'pa',
      FINANCIAL: 'financial', TRIAGE: 'triage', ADHERENCE: 'adherence', CLOSED: 'triage'
    };
    return map[stage] || 'triage';
  }

  getStageLabel(stage: string): string {
    const map: Record<string, string> = {
      INTAKE: 'Intake', CONSENT: 'Consent', BI_BV: 'Benefit Investigation', PA: 'Prior Auth',
      FINANCIAL: 'Financial', TRIAGE: 'SP Triage', ADHERENCE: 'Adherence', CLOSED: 'Closed'
    };
    return map[stage] || stage;
  }

  getPriorityClass(priority: string): string {
    const map: Record<string, string> = { HIGH: 'high', MEDIUM: 'med', LOW: 'low' };
    return map[priority] || 'low';
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }
}
