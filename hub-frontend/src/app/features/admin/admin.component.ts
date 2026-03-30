import { Component, OnInit, signal, inject } from '@angular/core';
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
}
