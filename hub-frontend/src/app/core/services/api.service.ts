import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse, CaseDetail, CaseListItem, Dashboard, ActionResult, CaseTask, TimelineEntry } from '../models/case.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<Dashboard> {
    return this.http.get<ApiResponse<Dashboard>>(`${this.baseUrl}/dashboard`).pipe(map(r => r.data));
  }

  getCases(params?: Record<string, string>): Observable<CaseListItem[]> {
    return this.http.get<ApiResponse<CaseListItem[]>>(`${this.baseUrl}/cases`, { params }).pipe(map(r => r.data));
  }

  getCaseDetail(id: string): Observable<CaseDetail> {
    return this.http.get<ApiResponse<CaseDetail>>(`${this.baseUrl}/cases/${id}`).pipe(map(r => r.data));
  }

  getTimeline(caseId: string, limit = 50): Observable<TimelineEntry[]> {
    return this.http.get<ApiResponse<TimelineEntry[]>>(`${this.baseUrl}/cases/${caseId}/timeline`, { params: { limit: limit.toString() } }).pipe(map(r => r.data));
  }

  performAction(caseId: string, actionKey: string, payload?: Record<string, unknown>): Observable<ActionResult> {
    return this.http.post<ApiResponse<ActionResult>>(`${this.baseUrl}/cases/${caseId}/actions/${actionKey}`, payload || {}).pipe(map(r => r.data));
  }

  getMyTasks(): Observable<CaseTask[]> {
    return this.http.get<ApiResponse<CaseTask[]>>(`${this.baseUrl}/tasks/mine`).pipe(map(r => r.data));
  }

  completeTask(caseId: string, taskId: string): Observable<CaseTask> {
    return this.http.patch<ApiResponse<CaseTask>>(`${this.baseUrl}/cases/${caseId}/tasks/${taskId}`,
      { completedAt: new Date().toISOString() }).pipe(map(r => r.data));
  }

  getManufacturers(): Observable<any[]> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/manufacturers`).pipe(map(r => r.data));
  }

  getManufacturer(id: string): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/manufacturers/${id}`).pipe(map(r => r.data));
  }

  getPrograms(): Observable<any[]> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/programs`).pipe(map(r => r.data));
  }

  getProgramConfig(programId: string): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/programs/${programId}/config`).pipe(map(r => r.data));
  }

  updateProgramConfig(programId: string, config: any): Observable<any> {
    return this.http.put<ApiResponse<any>>(`${this.baseUrl}/programs/${programId}/config`, config).pipe(map(r => r.data));
  }

  getUsers(): Observable<any[]> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/users`).pipe(map(r => r.data));
  }

  inviteUser(body: any): Observable<any> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/users/invite`, body).pipe(map(r => r.data));
  }
}
