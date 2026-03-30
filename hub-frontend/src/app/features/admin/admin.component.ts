import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);

  manufacturers = signal<any[]>([]);
  programs = signal<any[]>([]);
  selectedMfr = signal<any>(null);
  selectedProgram = signal<any>(null);
  config = signal<any>(null);
  users = signal<any[]>([]);
  saving = signal(false);
  saveSuccess = signal(false);
  showInviteModal = signal(false);
  inviteEmail = '';
  inviteRole = 'CASE_MANAGER';
  inviteFirstName = '';
  inviteLastName = '';
  inviteResult = signal<any>(null);
  editingPasswordUserId = signal<string | null>(null);
  newPassword = '';
  passwordSaved = signal<string | null>(null);
  showEditUserModal = signal(false);
  editUser: any = {};
  editUserSaved = signal(false);

  // Manufacturer modal
  showMfrModal = signal(false);
  mfrModalMode = signal<'create' | 'edit'>('create');
  mfrForm: any = {};
  mfrError = signal<string | null>(null);
  mfrSaving = signal(false);

  // Program wizard
  showProgramWizard = signal(false);
  programWizardStep = signal(1);
  programForm: any = {};
  programConfig: any = {};
  programSaving = signal(false);
  programError = signal<string | null>(null);

  // All manufacturers for program wizard dropdown
  allManufacturers = computed(() => this.manufacturers());

  // User program assignment
  expandedUserId = signal<string | null>(null);
  userProgramAssignments = signal<any[]>([]);
  loadingAssignments = signal(false);
  showAssignProgramModal = signal(false);
  assignProgramId = '';
  assignPermissions: any = { canView: true, canEdit: false, canManage: false };

  isAdmin = () => this.auth.user()?.roles?.includes('HUB_ADMIN') ?? false;

  // All programs across all manufacturers for assignment dropdown
  allPrograms = signal<any[]>([]);

  ngOnInit() {
    this.api.getManufacturers().subscribe(data => this.manufacturers.set(data));
    if (this.isAdmin()) {
      this.api.getUsers().subscribe(data => this.users.set(data));
    }
  }

  selectMfr(mfr: any) {
    this.selectedMfr.set(mfr);
    this.selectedProgram.set(null);
    this.config.set(null);
    this.api.getManufacturer(mfr.id).subscribe(data => this.programs.set(data.programs || []));
  }

  selectProgram(prog: any) {
    this.selectedProgram.set(prog);
    this.api.getProgramConfig(prog.id).subscribe(data => this.config.set(data));
  }

  saveConfig() {
    const c = this.config();
    if (!c || !this.selectedProgram()) return;
    this.saving.set(true);
    this.api.updateProgramConfig(this.selectedProgram().id, c).subscribe({
      next: (data) => { this.config.set(data); this.saving.set(false); this.saveSuccess.set(true); setTimeout(() => this.saveSuccess.set(false), 3000); },
      error: () => this.saving.set(false)
    });
  }

  inviteUser() {
    this.api.inviteUser({ email: this.inviteEmail, firstName: this.inviteFirstName, lastName: this.inviteLastName, role: this.inviteRole }).subscribe({
      next: (data) => {
        this.inviteResult.set(data);
        this.api.getUsers().subscribe(u => this.users.set(u));
      }
    });
  }

  startEditPassword(userId: string) {
    this.editingPasswordUserId.set(userId);
    this.newPassword = '';
    this.passwordSaved.set(null);
  }

  savePassword(userId: string) {
    if (!this.newPassword || this.newPassword.length < 6) return;
    this.api.updateUserPassword(userId, this.newPassword).subscribe({
      next: () => {
        this.editingPasswordUserId.set(null);
        this.newPassword = '';
        this.passwordSaved.set(userId);
        setTimeout(() => this.passwordSaved.set(null), 3000);
      }
    });
  }

  cancelEditPassword() {
    this.editingPasswordUserId.set(null);
    this.newPassword = '';
  }

  openEditUser(u: any) {
    this.editUser = { id: u.id, firstName: u.firstName, lastName: u.lastName, role: u.roles?.[0] || 'CASE_MANAGER', isActive: u.isActive, password: '' };
    this.editUserSaved.set(false);
    this.showEditUserModal.set(true);
  }

  saveEditUser() {
    const body: any = { firstName: this.editUser.firstName, lastName: this.editUser.lastName, isActive: this.editUser.isActive, role: this.editUser.role };
    if (this.editUser.password && this.editUser.password.length >= 6) {
      body.password = this.editUser.password;
    }
    this.api.updateUser(this.editUser.id, body).subscribe({
      next: () => {
        this.editUserSaved.set(true);
        this.api.getUsers().subscribe(u => this.users.set(u));
        setTimeout(() => this.showEditUserModal.set(false), 1500);
      }
    });
  }

  // --- Manufacturer Modal ---

  openCreateMfr() {
    this.mfrModalMode.set('create');
    this.mfrForm = { name: '', primaryContactName: '', primaryContactEmail: '', primaryContactPhone: '', contractReference: '', notes: '' };
    this.mfrError.set(null);
    this.mfrSaving.set(false);
    this.showMfrModal.set(true);
  }

  openEditMfr(mfr: any, event: Event) {
    event.stopPropagation();
    this.mfrModalMode.set('edit');
    this.mfrForm = {
      id: mfr.id,
      name: mfr.name || '',
      primaryContactName: mfr.primaryContactName || '',
      primaryContactEmail: mfr.primaryContactEmail || '',
      primaryContactPhone: mfr.primaryContactPhone || '',
      contractReference: mfr.contractReference || '',
      notes: mfr.notes || ''
    };
    this.mfrError.set(null);
    this.mfrSaving.set(false);
    this.showMfrModal.set(true);
  }

  saveMfr() {
    if (!this.mfrForm.name?.trim()) return;
    this.mfrSaving.set(true);
    this.mfrError.set(null);

    const body = {
      name: this.mfrForm.name,
      primaryContactName: this.mfrForm.primaryContactName,
      primaryContactEmail: this.mfrForm.primaryContactEmail,
      primaryContactPhone: this.mfrForm.primaryContactPhone,
      contractReference: this.mfrForm.contractReference,
      notes: this.mfrForm.notes
    };

    const request$ = this.mfrModalMode() === 'create'
      ? this.api.createManufacturer(body)
      : this.api.updateManufacturer(this.mfrForm.id, body);

    request$.subscribe({
      next: () => {
        this.mfrSaving.set(false);
        this.showMfrModal.set(false);
        this.api.getManufacturers().subscribe(data => this.manufacturers.set(data));
      },
      error: (err: any) => {
        this.mfrSaving.set(false);
        if (err.status === 409) {
          this.mfrError.set('A manufacturer with this name already exists.');
        } else {
          this.mfrError.set('An error occurred. Please try again.');
        }
      }
    });
  }

  // --- Program Wizard ---

  openCreateProgram() {
    this.programWizardStep.set(1);
    this.programForm = {
      manufacturerId: this.selectedMfr()?.id || '',
      name: '',
      drugBrandName: '',
      drugGenericName: '',
      therapeuticArea: '',
      programStartDate: ''
    };
    this.programConfig = {
      // Workflow
      priorAuthRequired: true,
      copayAssistanceEnabled: false,
      papEnabled: false,
      bridgeSupplyEnabled: false,
      ebvEnabled: false,
      eivEnabled: false,
      // Financial
      fplThresholdPct: 400,
      // Enrollment
      enrollmentAutoApprove: false,
      requirePhysicianSignature: true,
      requireConsentForm: true,
      // PA SLA
      paSubmitSlaBusinessDays: 5,
      paAppealWindowDays: 30,
      paFollowUpIntervalDays: 7,
      // Adherence
      adherenceProgramEnabled: false,
      adherenceCheckIntervalDays: 30,
      adherenceAlertThresholdPct: 80,
      // Additional Services
      nursingSupport: false,
      financialCounseling: false,
      patientEducation: false
    };
    this.programSaving.set(false);
    this.programError.set(null);
    this.showProgramWizard.set(true);
  }

  programWizardNext() {
    if (!this.programForm.manufacturerId || !this.programForm.name?.trim()) return;
    this.programWizardStep.set(2);
  }

  programWizardBack() {
    this.programWizardStep.set(1);
  }

  saveProgram() {
    this.programSaving.set(true);
    this.programError.set(null);

    const body = {
      name: this.programForm.name,
      drugBrandName: this.programForm.drugBrandName,
      drugGenericName: this.programForm.drugGenericName,
      therapeuticArea: this.programForm.therapeuticArea,
      programStartDate: this.programForm.programStartDate || null,
      config: this.programConfig
    };

    this.api.createProgram(this.programForm.manufacturerId, body).subscribe({
      next: () => {
        this.programSaving.set(false);
        this.showProgramWizard.set(false);
        // Refresh programs if manufacturer is selected
        if (this.selectedMfr()) {
          this.api.getManufacturer(this.selectedMfr().id).subscribe(data => this.programs.set(data.programs || []));
        }
        // Refresh manufacturers to update counts
        this.api.getManufacturers().subscribe(data => this.manufacturers.set(data));
      },
      error: (err: any) => {
        this.programSaving.set(false);
        this.programError.set(err.error?.message || 'An error occurred creating the program.');
      }
    });
  }

  // --- User Program Assignments ---

  toggleUserExpansion(user: any) {
    if (this.expandedUserId() === user.id) {
      this.expandedUserId.set(null);
      this.userProgramAssignments.set([]);
      return;
    }
    this.expandedUserId.set(user.id);
    this.loadUserAssignments(user.id);
  }

  private loadUserAssignments(userId: string) {
    this.loadingAssignments.set(true);
    // Load all programs to build the list, then check assignments
    // We load assignments for all programs and filter by user
    this.loadingAssignments.set(true);
    this.userProgramAssignments.set([]);

    // Build the flat program list from manufacturers
    const mfrs = this.manufacturers();
    const programFetches: any[] = [];
    let loadedPrograms: any[] = [];

    // Load all manufacturer details to get programs
    let remaining = mfrs.length;
    if (remaining === 0) {
      this.loadingAssignments.set(false);
      return;
    }

    const allProgs: any[] = [];
    mfrs.forEach(mfr => {
      this.api.getManufacturer(mfr.id).subscribe({
        next: (data) => {
          const progs = (data.programs || []).map((p: any) => ({ ...p, manufacturerName: mfr.name }));
          allProgs.push(...progs);
          remaining--;
          if (remaining === 0) {
            this.allPrograms.set(allProgs);
            this.loadAssignmentsForUser(userId, allProgs);
          }
        },
        error: () => {
          remaining--;
          if (remaining === 0) {
            this.allPrograms.set(allProgs);
            this.loadAssignmentsForUser(userId, allProgs);
          }
        }
      });
    });
  }

  private loadAssignmentsForUser(userId: string, allPrograms: any[]) {
    const assignments: any[] = [];
    let remaining = allPrograms.length;

    if (remaining === 0) {
      this.userProgramAssignments.set([]);
      this.loadingAssignments.set(false);
      return;
    }

    allPrograms.forEach(prog => {
      this.api.getProgramUsers(prog.id).subscribe({
        next: (users) => {
          const match = users.find((u: any) => u.userId === userId || u.id === userId);
          if (match) {
            assignments.push({
              programId: prog.id,
              programName: prog.name,
              manufacturerName: prog.manufacturerName,
              canView: match.canView ?? true,
              canEdit: match.canEdit ?? false,
              canManage: match.canManage ?? false
            });
          }
          remaining--;
          if (remaining === 0) {
            this.userProgramAssignments.set(assignments);
            this.loadingAssignments.set(false);
          }
        },
        error: () => {
          remaining--;
          if (remaining === 0) {
            this.userProgramAssignments.set(assignments);
            this.loadingAssignments.set(false);
          }
        }
      });
    });
  }

  openAssignProgram() {
    this.assignProgramId = '';
    this.assignPermissions = { canView: true, canEdit: false, canManage: false };
    this.showAssignProgramModal.set(true);
  }

  submitAssignProgram() {
    const userId = this.expandedUserId();
    if (!userId || !this.assignProgramId) return;

    const body = {
      userId,
      canView: this.assignPermissions.canView,
      canEdit: this.assignPermissions.canEdit,
      canManage: this.assignPermissions.canManage
    };

    this.api.assignUserToProgram(this.assignProgramId, body).subscribe({
      next: () => {
        this.showAssignProgramModal.set(false);
        this.loadUserAssignments(userId);
      }
    });
  }

  removeAssignment(assignment: any) {
    const userId = this.expandedUserId();
    if (!userId) return;

    this.api.removeUserFromProgram(assignment.programId, userId).subscribe({
      next: () => {
        this.loadUserAssignments(userId);
      }
    });
  }

  // Filtered list of programs not already assigned to the expanded user
  unassignedPrograms() {
    const assigned = new Set(this.userProgramAssignments().map(a => a.programId));
    return this.allPrograms().filter(p => !assigned.has(p.id));
  }
}
