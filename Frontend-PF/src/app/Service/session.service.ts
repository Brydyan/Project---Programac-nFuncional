import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SessionService {

  private api = '/app/v1/sessions';

  //  aquí guardamos la sesión actual
  private _session$ = new BehaviorSubject<any | null>(null);
  public session$ = this._session$.asObservable();

  constructor(private http: HttpClient) {}

  // acceso sincrónico al valor actual
  get currentSession(): any | null {
    return this._session$.value;
  }

  // Refrescar actividad por token (igual que antes)
  refreshActivity(): Observable<any> {
    const token = localStorage.getItem('token');
    if (!token) return new Observable();
    return this.http.post(`${this.api}/refresh/${token}`, {});
  }

  //  Obtener sesión por token Y guardarla globalmente
  getByToken(token: string): Observable<any> {
    return this.http.get(`${this.api}/token/${token}`).pipe(
      tap(session => this._session$.next(session))
    );
  }

  // Logout por sessionId (igual que antes)
  logout(sessionId: string): Observable<any> {
    return this.http.post(`${this.api}/logout/${sessionId}`, {});
  }

  // Marcar online/inactive por sessionId
  markOnline(sessionId: string): Observable<any> {
    if (!sessionId) return new Observable();
    return this.http.post(`${this.api}/online/${sessionId}`, {});
  }

  markInactive(sessionId: string): Observable<any> {
    if (!sessionId) return new Observable();
    return this.http.post(`${this.api}/inactive/${sessionId}`, {});
  }
}
