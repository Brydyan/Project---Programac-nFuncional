import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SessionService {


  private api = `${location.protocol}//${location.hostname}:8081/app/v1/sessions`;

  constructor(private http: HttpClient) {}

  // Refresh activity by token
  refreshActivity(): Observable<any> {
    const token = localStorage.getItem('token');
    if (!token) return new Observable();

    return this.http.post(`${this.api}/refresh/${token}`, {});
  }

  // Get session info by token
  getByToken(token: string): Observable<any> {
    return this.http.get(`${this.api}/token/${token}`);
  }

  // Logout a session by sessionId
  logout(sessionId: string): Observable<any> {
    return this.http.post(`${this.api}/logout/${sessionId}`, {});
  }
}
