import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { CaseTask } from '../../core/models/case.model';

@Component({
  selector: 'app-task-queue',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './task-queue.component.html',
  styleUrl: './task-queue.component.css'
})
export class TaskQueueComponent implements OnInit {
  private api = inject(ApiService);
  router = inject(Router);

  tasks = signal<CaseTask[]>([]);
  loading = signal(true);
  activeFilter = signal('all');

  filteredTasks = computed(() => {
    const all = this.tasks();
    const filter = this.activeFilter();
    switch (filter) {
      case 'high': return all.filter(t => t.priority === 'HIGH');
      case 'today': return all.filter(t => t.dueAt && new Date(t.dueAt).toDateString() === new Date().toDateString());
      case 'overdue': return all.filter(t => t.slaBreached || (t.dueAt && new Date(t.dueAt) < new Date()));
      default: return all;
    }
  });

  ngOnInit() {
    this.api.getMyTasks().subscribe({
      next: (data) => { this.tasks.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  setFilter(f: string) { this.activeFilter.set(f); }

  completeTask(task: CaseTask, event: Event) {
    event.stopPropagation();
    this.api.completeTask(task.caseId, task.id).subscribe(() => {
      this.tasks.update(tasks => tasks.filter(t => t.id !== task.id));
    });
  }

  openCase(task: CaseTask) { this.router.navigate(['/cases', task.caseId]); }

  getPriorityClass(p: string): string {
    return p === 'HIGH' ? 'high' : p === 'MEDIUM' ? 'med' : 'low';
  }

  formatDate(d: string | null): string {
    if (!d) return '';
    return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  isOverdue(d: string | null): boolean {
    return !!d && new Date(d) < new Date();
  }
}
