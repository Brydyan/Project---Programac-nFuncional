import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { routes } from './app.routes';
import { AuthInterceptor } from './Service/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // 👇 Modo ZONELESS (sin Zone.js)
    provideZonelessChangeDetection(),

    provideRouter(routes),
    provideHttpClient(),

    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ]
};