import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, switchMap, throwError } from 'rxjs';

export interface RegisterPayload {
  email: string;
  nombre: string;
  alias: string;
  password: string;
  day?: string;
  month?: string;
  year?: string;
}

export interface UserEntity {
  id?: string;
  username: string;
  displayName?: string;
  email: string;
  password?: string;
}

export interface SessionPayload {
  userId: string;
  token: string;
  status?: string;
  device?: string;
  ipAddress?: string;
  location?: string;
  browser?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = '/app/v1';

  constructor(private http: HttpClient) {}

  // Registra un nuevo usuario en el backend
  register(payload: RegisterPayload): Observable<UserEntity> {
    const user: UserEntity = {
      username: payload.alias,
      displayName: payload.nombre,
      email: payload.email,
      password: payload.password
    };
    return this.http.post<UserEntity>(`${this.baseUrl}/user`, user);
  }

  // Crea una sesión en el backend. Busca el usuario por email o username
  // Autenticación: llama al endpoint de login que devuelve un JWT
  login(identifier: string, password?: string): Observable<any> {
    const body = { identifier, password };
    return this.http.post<any>(`${this.baseUrl}/auth/login`, body).pipe(
      map((resp: any) => {
        const token = resp?.token;
        if (token) {
          this.saveToken(token);
        }
        return resp;
      })
    );
  }

  forgotPassword(email: string): Observable<UserEntity | null> {
    return this.http.get<UserEntity[]>(`${this.baseUrl}/user`).pipe(
      map(users => users.find(u => u.email === email) || null)
    );
  }

  saveToken(token: string) {
    try {
      localStorage.setItem('app_token', token);
    } catch (e) {
      console.warn('No se pudo guardar token en localStorage', e);
    }
  }

  getToken(): string | null {
    try {
      return localStorage.getItem('app_token');
    } catch (e) {
      return null;
    }
  }

  private generateToken(): string {
    // Token corto para sesiones de ejemplo
    return Math.random().toString(36).substring(2) + Date.now().toString(36);
  }
}
