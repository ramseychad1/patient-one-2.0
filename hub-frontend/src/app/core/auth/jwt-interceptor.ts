import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { DemoScenarioService } from '../services/demo-scenario.service';
import { tap } from 'rxjs/operators';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const scenarioService = inject(DemoScenarioService);

  let headers = req.headers;

  const token = authService.getToken();
  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }

  const scenario = scenarioService.scenario();
  if (scenario && scenario !== 'DEFAULT') {
    headers = headers.set('X-Demo-Scenario', scenario);
  }

  return next(req.clone({ headers })).pipe(
    tap({
      error: (err) => {
        if (err.status === 401 && !req.url.includes('/auth/login')) {
          // Token expired or invalid — clear session and redirect to login
          authService.logout();
        }
      }
    })
  );
};
