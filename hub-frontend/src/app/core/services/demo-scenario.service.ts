import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class DemoScenarioService {
  private readonly STORAGE_KEY = 'ha_demo_scenario';

  readonly scenario = signal<string>(this.load());

  setScenario(name: string) {
    localStorage.setItem(this.STORAGE_KEY, name);
    this.scenario.set(name);
  }

  private load(): string {
    return localStorage.getItem(this.STORAGE_KEY) || 'DEFAULT';
  }
}
