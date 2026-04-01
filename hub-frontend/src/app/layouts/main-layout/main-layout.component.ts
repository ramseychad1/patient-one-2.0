import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { DemoScenarioService } from '../../core/services/demo-scenario.service';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private api = inject(ApiService);
  private router = inject(Router);
  scenarioService = inject(DemoScenarioService);

  scenarioOpen = false;
  programSwitcherOpen = false;
  scenarios = [
    { key: 'DEFAULT', desc: 'Commercial, PA required', dotClass: 'dot-green' },
    { key: 'GOVERNMENT_PATIENT', desc: 'Medicare, copay blocked (AKS)', dotClass: 'dot-amber' },
    { key: 'UNINSURED_PATIENT', desc: 'No insurance, PAP pathway', dotClass: 'dot-purple' },
    { key: 'PA_DENIED', desc: 'PA denied, triggers appeal', dotClass: 'dot-red' },
    { key: 'EIV_INELIGIBLE', desc: 'Income too high for PAP', dotClass: 'dot-gray' },
  ];

  myPrograms = signal<any[]>([]);
  activeProgram = signal<any>(null);

  user = this.authService.user;
  initials = computed(() => {
    const u = this.user();
    return u ? (u.firstName[0] + u.lastName[0]).toUpperCase() : '??';
  });
  fullName = computed(() => {
    const u = this.user();
    return u ? `${u.firstName} ${u.lastName}` : '';
  });
  role = computed(() => {
    const u = this.user();
    return u?.roles?.[0]?.replace(/_/g, ' ') || '';
  });
  scenarioDotClass = computed(() => {
    const s = this.scenarioService.scenario();
    return this.scenarios.find(sc => sc.key === s)?.dotClass || 'dot-green';
  });
  showProgramSwitcher = computed(() => this.myPrograms().length > 1);

  ngOnInit() {
    this.api.getMyPrograms().subscribe({
      next: (programs) => {
        this.myPrograms.set(programs);
        const savedId = localStorage.getItem('ha_active_program_id');
        const match = savedId ? programs.find((p: any) => p.id === savedId) : null;
        const active = match || programs[0] || null;
        this.activeProgram.set(active);

        // Ensure the JWT has the activeProgramId claim.
        // After login the JWT won't include it, so re-set it and reload once
        // so the dashboard fetches program-scoped data.
        if (active && !this.jwtHasActiveProgram(active.id)) {
          this.api.setActiveProgram(active.id).subscribe({
            next: (res) => {
              if (res.accessToken) {
                localStorage.setItem('ha_access_token', res.accessToken);
              }
              localStorage.setItem('ha_active_program_id', active.id);
              // Reload so all components use the updated JWT
              window.location.reload();
            }
          });
        }
      }
    });
  }

  private jwtHasActiveProgram(expectedId: string): boolean {
    const token = localStorage.getItem('ha_access_token');
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.activeProgramId === expectedId;
    } catch {
      return false;
    }
  }

  switchProgram(program: any) {
    this.api.setActiveProgram(program.id).subscribe({
      next: (res) => {
        // Store new JWT with activeProgramId claim
        if (res.accessToken) {
          localStorage.setItem('ha_access_token', res.accessToken);
        }
        localStorage.setItem('ha_active_program_id', program.id);
        this.programSwitcherOpen = false;
        // Navigate to dashboard with full page reload to reset all components
        window.location.href = '/dashboard';
      }
    });
  }

  getProgramDotClass(index: number): string {
    return ['dot-teal', 'dot-purple', 'dot-blue', 'dot-amber'][index % 4];
  }

  setScenario(key: string) {
    this.scenarioService.setScenario(key);
    this.scenarioOpen = false;
  }

  logout() {
    this.authService.logout();
  }
}
