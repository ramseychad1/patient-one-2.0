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
        // Set first as active if not already set
        if (programs.length > 0 && !this.activeProgram()) {
          this.activeProgram.set(programs[0]);
        }
      }
    });
  }

  switchProgram(program: any) {
    this.api.setActiveProgram(program.id).subscribe({
      next: (res) => {
        // Store new JWT with activeProgramId claim
        if (res.accessToken) {
          localStorage.setItem('ha_access_token', res.accessToken);
        }
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
