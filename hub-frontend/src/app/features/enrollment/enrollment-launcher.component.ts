import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { ApiResponse, CaseDetail } from '../../core/models/case.model';

@Component({
  selector: 'app-enrollment-launcher',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './enrollment-launcher.component.html',
  styleUrl: './enrollment-launcher.component.css'
})
export class EnrollmentLauncherComponent {
  private router = inject(Router);
  private http = inject(HttpClient);

  activeModal = signal<string | null>(null);
  submitting = signal(false);
  aksWarning = signal(false);

  // DEP form fields
  dep = {
    firstName: '', lastName: '', dob: '', phone: '', preferredContactMethod: 'PHONE',
    npi: '', prescriberFirst: '', prescriberLast: '', practiceName: '', prescriberPhone: '', prescriberFax: '',
    insuranceType: 'COMMERCIAL', planName: '', memberId: '', groupNumber: '',
    ndcCode: '12345-678-90', brandName: 'Velarix', diagnosisCode: ''
  };

  // eRX simulated fields
  erx = {
    transactionId: 'SS-2026-' + Math.floor(Math.random() * 99999).toString().padStart(5, '0'),
    patientFirst: 'New', patientLast: 'Patient', dob: '1980-01-15', phone: '614-555-0100',
    npi: '1234567890', prescriberFirst: 'Tran', prescriberLast: 'Nguyen',
    drug: 'Velarix (velarixumab)', ndcCode: '12345-678-90'
  };

  openModal(source: string) { this.activeModal.set(source); }
  closeModal() { this.activeModal.set(null); this.aksWarning.set(false); }

  onInsuranceTypeChange() {
    const govTypes = ['MEDICARE', 'MEDICAID', 'TRICARE', 'VA', 'MEDICARE_ADVANTAGE'];
    this.aksWarning.set(govTypes.includes(this.dep.insuranceType));
  }

  submitDep() {
    this.submitting.set(true);
    const body = {
      enrollmentSource: 'PORTAL',
      programId: '22222222-2222-2222-2222-222222222222',
      patient: { firstName: this.dep.firstName, lastName: this.dep.lastName, dob: this.dep.dob, phone: this.dep.phone, preferredContactMethod: this.dep.preferredContactMethod },
      prescriber: { npi: this.dep.npi || '1234567890', firstName: this.dep.prescriberFirst, lastName: this.dep.prescriberLast, practiceName: this.dep.practiceName, phone: this.dep.prescriberPhone, fax: this.dep.prescriberFax },
      drug: { ndcCode: this.dep.ndcCode, brandName: this.dep.brandName, diagnosisCode: this.dep.diagnosisCode },
      insurance: { insuranceType: this.dep.insuranceType, planName: this.dep.planName, memberId: this.dep.memberId, groupNumber: this.dep.groupNumber },
      miFlags: []
    };
    this.createCase(body);
  }

  submitErx() {
    this.submitting.set(true);
    const body = {
      enrollmentSource: 'ERX',
      programId: '22222222-2222-2222-2222-222222222222',
      patient: { firstName: this.erx.patientFirst, lastName: this.erx.patientLast, dob: this.erx.dob, phone: this.erx.phone, preferredContactMethod: 'PHONE' },
      prescriber: { npi: this.erx.npi, firstName: this.erx.prescriberFirst, lastName: this.erx.prescriberLast },
      drug: { ndcCode: this.erx.ndcCode, brandName: 'Velarix' },
      insurance: {},
      miFlags: ['insurance_member_id', 'diagnosis_code'],
      erxTransactionId: this.erx.transactionId
    };
    this.createCase(body);
  }

  submitFax() {
    this.submitting.set(true);
    const body = {
      enrollmentSource: 'FAX_PDF',
      programId: '22222222-2222-2222-2222-222222222222',
      patient: { firstName: 'Fax', lastName: 'Patient', dob: '1975-06-20', phone: '614-555-0300', preferredContactMethod: 'PHONE' },
      prescriber: { npi: '1234567890' },
      drug: { ndcCode: '12345-678-90', brandName: 'Velarix' },
      insurance: {},
      miFlags: ['insurance_member_id', 'group_number', 'diagnosis_code']
    };
    this.createCase(body);
  }

  private createCase(body: any) {
    this.http.post<ApiResponse<CaseDetail>>(`${environment.apiBaseUrl}/cases`, body).subscribe({
      next: (res) => {
        this.submitting.set(false);
        this.closeModal();
        this.router.navigate(['/cases', res.data.id]);
      },
      error: () => this.submitting.set(false)
    });
  }
}
