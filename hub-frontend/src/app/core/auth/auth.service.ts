import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse, UserInfo } from '../models/auth.model';
import { ApiResponse } from '../models/case.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'ha_access_token';
  private readonly REFRESH_KEY = 'ha_refresh_token';
  private readonly USER_KEY = 'ha_user';

  private userSignal = signal<UserInfo | null>(this.loadUser());

  readonly user = this.userSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.userSignal() && !!this.getToken());

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest) {
    return this.http.post<ApiResponse<LoginResponse>>(
      `${environment.apiBaseUrl}/auth/login`, credentials
    );
  }

  handleLoginSuccess(response: ApiResponse<LoginResponse>) {
    const { accessToken, refreshToken, user } = response.data;
    localStorage.setItem(this.TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_KEY, refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.userSignal.set(user);
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.userSignal.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_KEY);
  }

  refreshToken() {
    const refreshToken = this.getRefreshToken();
    return this.http.post<ApiResponse<{ accessToken: string }>>(
      `${environment.apiBaseUrl}/auth/refresh`, { refreshToken }
    );
  }

  private loadUser(): UserInfo | null {
    const stored = localStorage.getItem(this.USER_KEY);
    return stored ? JSON.parse(stored) : null;
  }
}
