import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { DemoScenarioService } from '../../core/services/demo-scenario.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent {
  private authService = inject(AuthService);
  scenarioService = inject(DemoScenarioService);

  scenarioOpen = false;
  scenarios = [
    { key: 'DEFAULT', desc: 'Commercial, PA required', dotClass: 'dot-green' },
    { key: 'GOVERNMENT_PATIENT', desc: 'Medicare, copay blocked (AKS)', dotClass: 'dot-amber' },
    { key: 'UNINSURED_PATIENT', desc: 'No insurance, PAP pathway', dotClass: 'dot-purple' },
    { key: 'PA_DENIED', desc: 'PA denied, triggers appeal', dotClass: 'dot-red' },
    { key: 'EIV_INELIGIBLE', desc: 'Income too high for PAP', dotClass: 'dot-gray' },
  ];

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

  setScenario(key: string) {
    this.scenarioService.setScenario(key);
    this.scenarioOpen = false;
  }

  logout() {
    this.authService.logout();
  }
}
