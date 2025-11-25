import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap, switchMap } from 'rxjs/operators';

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

  // Refrescar actividad por sessionId
  refreshBySessionId(sessionId: string): Observable<any> {
    if (!sessionId) return new Observable();
    return this.http.post(`${this.api}/refresh/${sessionId}`, {});
  }

  // Refrescar actividad usando la sesión actual (usado por Dashboard)
  refreshActivity(): Observable<any> {
    const session = this.currentSession;
    const sessionId = session?.sessionId || session?.id;

    if (sessionId) {
      return this.refreshBySessionId(sessionId);
    }

    const token = localStorage.getItem('token');
    if (!token) return of(null);

    return this.getByToken(token).pipe(
      switchMap((s: any) => {
        const sid = s?.sessionId || s?.id;
        if (sid) return this.refreshBySessionId(sid);
        return of(null);
      })
    );
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

  //  Obtener sesión por token Y guardarla globalmente
  getByToken(token: string): Observable<any> {
    return this.http.get(`${this.api}/token/${token}`).pipe(
      tap(session => this._session$.next(session))
    );
  }

  // Obtener todas las sesiones asociadas a un usuario
  getByUserId(userId: string) {
    if (!userId) return new Observable();
    return this.http.get<any[]>(`${this.api}/user/${userId}`);
  }

  // Logout por sessionId (igual que antes)
  logout(sessionId: string): Observable<any> {
    return this.http.post(`${this.api}/logout/${sessionId}`, {});
  }
}
