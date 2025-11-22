import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

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

@Injectable({ providedIn: 'root' })
export class AuthService {

  // API base: use the current host so mobile devices accessing the app via PC IP work
  private api = `${location.protocol}//${location.hostname}:8081/app/v1`;

  constructor(private http: HttpClient) {}

  // ============================
  //  🔵 LOGIN
  // ============================
  login(data: { identifier: string; password: string }): Observable<any> {
    return this.http.post(`${this.api}/auth/login`, data);
  }

  // ============================
  //  🔵 REGISTRO
  // ============================
  register(payload: RegisterPayload): Observable<UserEntity> {
    const user: UserEntity = {
      username: payload.alias,
      displayName: payload.nombre,
      email: payload.email,
      password: payload.password
    };
    
    return this.http.post<UserEntity>(`${this.api}/user`, user);
  }

  // ============================
  //  🔵 RECUPERAR CONTRASEÑA
  // ============================
  forgotPassword(email: string): Observable<UserEntity | null> {
    return this.http.get<UserEntity[]>(`${this.api}/user`).pipe(
      map(users => users.find(u => u.email === email) || null)
    );
  }

  // ============================
  //  🔵 TOKEN
  // ============================
  saveToken(token: string) {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout() {
    localStorage.removeItem('token');
  }

  // ============================
  //  🔵 VALIDAR TOKEN (auto-login)
  // ============================
  validateToken(token: string) {
    if (!token) return this.http.get(`${this.api}/sessions/token/invalid-token`);
    return this.http.get(`${this.api}/sessions/token/${token}`);
  }
}
