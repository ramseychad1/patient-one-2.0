import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimelineEntry } from '../../../core/models/case.model';

@Component({
  selector: 'app-timeline-entry',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="tl-entry">
      <div class="tl-dot" [class]="'tl-dot--' + entry.entryType"></div>
      <div class="tl-content">
        <div class="tl-title">{{ entry.title }}</div>
        <div class="tl-meta">{{ entry.performedBy }} &middot; {{ entry.occurredAt | date:'short' }}</div>
        @if (entry.description) {
          <div class="tl-desc">{{ entry.description }}</div>
        }
      </div>
    </div>
  `,
  styles: [`
    .tl-entry { display: flex; gap: 12px; padding: 8px 0; }
    .tl-dot { width: 10px; height: 10px; border-radius: 50%; margin-top: 5px; flex-shrink: 0; }
    .tl-dot--STATUS_CHANGE { background: var(--blue-accent); }
    .tl-dot--INTERACTION { background: var(--success-green); }
    .tl-dot--OUTREACH { background: var(--warning-amber); }
    .tl-title { font-weight: 600; font-size: 0.875rem; }
    .tl-meta { font-size: 0.75rem; color: var(--warm-gray-7); }
    .tl-desc { font-size: 0.875rem; margin-top: 4px; color: #555; }
  `]
})
export class TimelineEntryComponent {
  @Input() entry!: TimelineEntry;
}
