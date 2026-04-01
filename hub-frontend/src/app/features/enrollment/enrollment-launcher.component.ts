import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { ApiResponse, CaseDetail } from '../../core/models/case.model';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-enrollment-launcher',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './enrollment-launcher.component.html',
  styleUrl: './enrollment-launcher.component.css'
})
export class EnrollmentLauncherComponent implements OnInit {
  private router = inject(Router);
  private http = inject(HttpClient);
  private api = inject(ApiService);

  activeModal = signal<string | null>(null);
  submitting = signal(false);
  aksWarning = signal(false);

  // Active program context
  activeProgramId = '';
  activeDrugBrand = '';
  activeDrugGeneric = '';
  activeProgramName = '';

  // DEP form fields
  dep = {
    firstName: '', lastName: '', dob: '', phone: '', preferredContactMethod: 'PHONE',
    npi: '', prescriberFirst: '', prescriberLast: '', practiceName: '', prescriberPhone: '', prescriberFax: '',
    insuranceType: 'COMMERCIAL', planName: '', memberId: '', groupNumber: '',
    ndcCode: '12345-678-90', brandName: '', diagnosisCode: ''
  };

  // eRX simulated fields
  erx = {
    transactionId: 'SS-2026-' + Math.floor(Math.random() * 99999).toString().padStart(5, '0'),
    patientFirst: 'Demo', patientLast: '', dob: '1980-01-15', phone: '614-555-0100',
    npi: '1234567890', prescriberFirst: 'Tran', prescriberLast: 'Nguyen',
    drug: '', ndcCode: '12345-678-90'
  };

  private nextDemoSuffix(): string {
    const key = 'ha_demo_patient_seq';
    const seq = parseInt(localStorage.getItem(key) || '0', 10) + 1;
    localStorage.setItem(key, seq.toString());
    return seq.toString().padStart(4, '0');
  }

  ngOnInit() {
    this.api.getMyPrograms().subscribe({
      next: (programs) => {
        const savedId = localStorage.getItem('ha_active_program_id');
        const active = savedId ? programs.find((p: any) => p.id === savedId) : programs[0];
        if (active) {
          this.activeProgramId = active.id;
          this.activeDrugBrand = active.drugBrandName || active.name;
          this.activeDrugGeneric = active.drugGenericName || '';
          this.activeProgramName = active.name;
          this.dep.brandName = this.activeDrugBrand;
          this.erx.drug = this.activeDrugGeneric
            ? `${this.activeDrugBrand} (${this.activeDrugGeneric})`
            : this.activeDrugBrand;
        }
      }
    });
  }

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
      programId: this.activeProgramId,
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
    const suffix = this.nextDemoSuffix();
    const body = {
      enrollmentSource: 'ERX',
      programId: this.activeProgramId,
      patient: { firstName: this.erx.patientFirst, lastName: 'Patient' + suffix, dob: this.erx.dob, phone: this.erx.phone, preferredContactMethod: 'PHONE' },
      prescriber: { npi: this.erx.npi, firstName: this.erx.prescriberFirst, lastName: this.erx.prescriberLast },
      drug: { ndcCode: this.erx.ndcCode, brandName: this.activeDrugBrand },
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
      programId: this.activeProgramId,
      patient: { firstName: 'Fax', lastName: 'Patient' + this.nextDemoSuffix(), dob: '1975-06-20', phone: '614-555-0300', preferredContactMethod: 'PHONE' },
      prescriber: { npi: '1234567890' },
      drug: { ndcCode: '12345-678-90', brandName: this.activeDrugBrand },
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
        // Store new case info for dashboard highlight
        localStorage.setItem('ha_new_case', JSON.stringify({
          id: res.data.id,
          caseNumber: res.data.caseNumber,
          patientName: res.data.patient?.firstName + ' ' + res.data.patient?.lastName,
          timestamp: Date.now()
        }));
        this.router.navigate(['/dashboard']);
      },
      error: () => this.submitting.set(false)
    });
  }
}
