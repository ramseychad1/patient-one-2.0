import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-pill',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="pill" [class]="'pill--' + variant">{{ label }}</span>`,
  styles: [`
    .pill { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; }
    .pill--success { background: #e6f7f1; color: var(--success-green); }
    .pill--warning { background: #fff5e6; color: var(--warning-amber); }
    .pill--danger { background: #fde8e8; color: var(--danger-red); }
    .pill--info { background: #e6f0fa; color: var(--blue-accent); }
    .pill--default { background: #f1efe8; color: var(--warm-gray-7); }
  `]
})
export class StatusPillComponent {
  @Input() label = '';
  @Input() variant: 'success' | 'warning' | 'danger' | 'info' | 'default' = 'default';
}
