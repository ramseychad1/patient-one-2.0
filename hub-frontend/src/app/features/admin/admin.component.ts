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

  // Invite wizard state
  inviteStep = signal(1);
  inviteForm: any = { email: '', firstName: '', lastName: '', role: 'CASE_MANAGER' };
  inviteAssignments: any[] = [];
  inviteResult = signal<any>(null);
  inviteSaving = signal(false);
  allPrograms = signal<any[]>([]);

  editingPasswordUserId = signal<string | null>(null);
  newPassword = '';
  passwordSaved = signal<string | null>(null);
  showEditUserModal = signal(false);
  editUser: any = {};
  editUserSaved = signal(false);

  // Edit user tabs & program access
  editUserTab = signal<'details' | 'programs'>('details');
  editUserPrograms = signal<any[]>([]);
  editUserLoadingPrograms = signal(false);
  showAddAssignment = signal(false);
  addAssignmentProgramId = '';
  addAssignmentPerms: any = { canCreateCases: true, canEditCases: true, canCloseCases: false, canViewFinancials: true, canPerformActions: true };

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

  // --- Invite Wizard ---

  openInviteWizard() {
    this.inviteStep.set(1);
    this.inviteForm = { email: '', firstName: '', lastName: '', role: 'CASE_MANAGER' };
    this.inviteAssignments = [];
    this.inviteResult.set(null);
    this.showInviteModal.set(true);
    // Load all programs for step 2
    this.api.getMyPrograms().subscribe(p => {
      this.allPrograms.set(p);
      this.inviteAssignments = p.map((prog: any) => ({
        programId: prog.id, programName: prog.name || prog.drugBrandName, manufacturerName: prog.manufacturerName,
        drugBrandName: prog.drugBrandName, checked: false,
        canCreateCases: true, canEditCases: true, canCloseCases: false, canViewFinancials: true, canPerformActions: true
      }));
    });
  }

  inviteNext() {
    if (this.inviteStep() === 1) {
      if (this.inviteForm.role === 'HUB_ADMIN') {
        this.submitInvite(); // Skip step 2 for admins
      } else {
        this.inviteStep.set(2);
      }
    } else if (this.inviteStep() === 2) {
      this.submitInvite();
    }
  }

  inviteBack() { this.inviteStep.update(s => Math.max(1, s - 1)); }

  submitInvite() {
    this.inviteSaving.set(true);
    const selected = this.inviteAssignments.filter((a: any) => a.checked).map((a: any) => ({
      programId: a.programId, canCreateCases: a.canCreateCases, canEditCases: a.canEditCases,
      canCloseCases: a.canCloseCases, canViewFinancials: a.canViewFinancials, canPerformActions: a.canPerformActions
    }));
    const body: any = { ...this.inviteForm, programAssignments: selected };
    this.api.inviteUserWithAssignments(body).subscribe({
      next: (data) => {
        this.inviteResult.set(data);
        this.inviteStep.set(3);
        this.inviteSaving.set(false);
        this.api.getUsers().subscribe(u => this.users.set(u));
      },
      error: () => this.inviteSaving.set(false)
    });
  }

  copyPassword() {
    navigator.clipboard.writeText(this.inviteResult()?.tempPassword || '');
  }

  checkedAssignments(): any[] {
    return this.inviteAssignments.filter((a: any) => a.checked);
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

  deleteUser(u: any) {
    if (!confirm(`Are you sure you want to delete ${u.firstName} ${u.lastName} (${u.email})? This cannot be undone.`)) return;
    this.api.updateUser(u.id, { isActive: false }).subscribe(() => {
      this.api.getUsers().subscribe(users => this.users.set(users));
    });
  }

  // --- Edit User (tabbed) ---

  openEditUser(u: any) {
    this.editUser = { id: u.id, firstName: u.firstName, lastName: u.lastName, email: u.email, role: u.roles?.[0] || 'CASE_MANAGER', isActive: u.isActive, isHubAdmin: u.isHubAdmin, password: '' };
    this.editUserTab.set('details');
    this.editUserSaved.set(false);
    this.showEditUserModal.set(true);
    this.loadEditUserPrograms(u.id);
  }

  loadEditUserPrograms(userId: string) {
    this.editUserLoadingPrograms.set(true);
    this.api.getUserPrograms(userId).subscribe({
      next: (data) => { this.editUserPrograms.set(data); this.editUserLoadingPrograms.set(false); },
      error: () => this.editUserLoadingPrograms.set(false)
    });
  }

  updatePermission(assignment: any, field: string, value: boolean) {
    const body: any = {};
    body[field] = value;
    this.api.updateProgramAssignment(assignment.programId, this.editUser.id, body).subscribe();
  }

  removeUserAssignment(assignment: any) {
    if (!confirm(`Remove ${this.editUser.firstName} from ${assignment.programName}?`)) return;
    this.api.removeUserFromProgram(assignment.programId, this.editUser.id).subscribe(() => {
      this.loadEditUserPrograms(this.editUser.id);
    });
  }

  submitAddAssignment() {
    this.api.assignUserToProgram(this.addAssignmentProgramId, {
      userId: this.editUser.id, ...this.addAssignmentPerms
    }).subscribe(() => {
      this.showAddAssignment.set(false);
      this.loadEditUserPrograms(this.editUser.id);
    });
  }

  unassignedProgramsForEdit(): any[] {
    const assigned = new Set(this.editUserPrograms().map((a: any) => a.programId));
    return this.allPrograms().filter((p: any) => !assigned.has(p.id));
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
      // Enrollment
      acceptedSources: ['FAX_PDF', 'ERX', 'DEP'],
      miSlaBusinessDays: 5,
      consentMethod: 'SMS',
      // Workflow
      priorAuthRequired: true,
      copayAssistanceEnabled: true,
      papEnabled: true,
      bridgeSupplyEnabled: true,
      quickStartEnabled: true,
      remsTrackingEnabled: false,
      adherenceProgramEnabled: true,
      ebvEnabled: true,
      eivEnabled: true,
      nurseEducationEnabled: false,
      welcomeKitEnabled: false,
      travelAssistanceEnabled: false,
      infusionSiteEnabled: false,
      // Financial
      fplThresholdPct: 400,
      papApprovalDurationMonths: 12,
      papReenrollmentLeadDays: 90,
      copayMaxBenefitUsd: null,
      bridgeMaxDurationMonths: 6,
      quickStartMaxDurationMonths: 12,
      // PA SLA
      paSubmitSlaBusinessDays: 3,
      paFollowupSlaBusinessDays: 5,
      paAppealWindowDays: 30,
      paMaxAppealLevels: 2,
      paAutoEscalate: true,
      // SP & Adherence
      spFollowupSlaBusinessDays: 5,
      adherenceCheckinInterval1: 30,
      adherenceCheckinInterval2: 60,
      adherenceCheckinInterval3: 90,
      refillReminderLeadDays: 7
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

    const configToSend = { ...this.programConfig };
    configToSend.adherenceCheckinIntervalsDays = [
      this.programConfig.adherenceCheckinInterval1,
      this.programConfig.adherenceCheckinInterval2,
      this.programConfig.adherenceCheckinInterval3
    ];
    delete configToSend.adherenceCheckinInterval1;
    delete configToSend.adherenceCheckinInterval2;
    delete configToSend.adherenceCheckinInterval3;

    const body = {
      name: this.programForm.name,
      drugBrandName: this.programForm.drugBrandName,
      drugGenericName: this.programForm.drugGenericName,
      therapeuticArea: this.programForm.therapeuticArea,
      programStartDate: this.programForm.programStartDate || null,
      config: configToSend
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
    this.userProgramAssignments.set([]);

    // Build the flat program list from manufacturers
    const mfrs = this.manufacturers();

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

  toggleSource(source: string) {
    const idx = this.programConfig.acceptedSources.indexOf(source);
    if (idx >= 0) this.programConfig.acceptedSources.splice(idx, 1);
    else this.programConfig.acceptedSources.push(source);
  }
  hasSource(source: string): boolean {
    return this.programConfig.acceptedSources?.includes(source) ?? false;
  }

  // Filtered list of programs not already assigned to the expanded user
  unassignedPrograms() {
    const assigned = new Set(this.userProgramAssignments().map(a => a.programId));
    return this.allPrograms().filter(p => !assigned.has(p.id));
  }
}
