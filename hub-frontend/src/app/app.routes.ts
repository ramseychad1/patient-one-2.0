import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { CasesListComponent } from './features/cases/cases-list.component';
import { CaseDetailComponent } from './features/cases/case-detail/case-detail.component';
import { TaskQueueComponent } from './features/tasks/task-queue.component';
import { AdminComponent } from './features/admin/admin.component';
import { EnrollmentLauncherComponent } from './features/enrollment/enrollment-launcher.component';

export const routes: Routes = [
  {
    path: '',
    component: AuthLayoutComponent,
    children: [
      { path: 'login', component: LoginComponent }
    ]
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'cases/new', component: EnrollmentLauncherComponent },
      { path: 'cases', component: CasesListComponent },
      { path: 'cases/:id', component: CaseDetailComponent },
      { path: 'tasks', component: TaskQueueComponent },
      { path: 'admin', component: AdminComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];
