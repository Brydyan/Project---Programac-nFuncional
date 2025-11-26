import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, Subscription, timer } from 'rxjs';
import { tap, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class SessionService {


  private api = '/app/v1/sessions';

  //  aquí guardamos la sesión actual
  private _session$ = new BehaviorSubject<any | null>(null);
  public session$ = this._session$.asObservable();

  private sessionWatcherSub: Subscription | null = null;
  private watchIntervalMs = 15000; // 15s - intervalo para verificar validez de sesión
  private presenceHeartbeatSub: Subscription | null = null;
  private presenceHeartbeatMs = 10000; // 10s - interval to refresh presence TTL

  constructor(private http: HttpClient, private router: Router) {}

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
    return this.http.get<any>(`${this.api}/token/${token}`).pipe(
      tap(session => {
        this._session$.next(session);
        // iniciar el watcher cuando se obtiene una sesión válida
        if (session && session.valid !== false) {
          this.startSessionWatcher();
          this.startPresenceHeartbeat();
        }
      })
    );
  }

  // Obtener todas las sesiones asociadas a un usuario
  getByUserId(userId: string) {
    if (!userId) return new Observable();
    return this.http.get<any[]>(`${this.api}/user/${userId}`);
  }

  // Logout por sessionId (igual que antes)
  logout(sessionId: string): Observable<any> {
    return this.http.post(`${this.api}/logout/${sessionId}`, {}).pipe(
      tap(() => {
        const cur = this.currentSession;
        const sid = cur?.sessionId || cur?.id;
        if (sid && (sid === sessionId)) {
          // Si cerramos la sesión actual, forzar salida local
          this.handleInvalidSession();
        }
      })
    );
  }

  // Logout todas las sesiones de un usuario
  logoutAll(userId: string): Observable<any> {
    if (!userId) return new Observable();
    return this.http.post(`${this.api}/logout/user/${userId}`, {}).pipe(
      tap(() => {
        const cur = this.currentSession;
        const uid = cur?.userId;
        if (uid && uid === userId) {
          this.handleInvalidSession();
        }
      })
    );
  }

  // Iniciar un watcher periódico para validar que la sesión actual siga existiendo.
  startSessionWatcher() {
    // si ya existe, no duplicar
    if (this.sessionWatcherSub) return;

    const token = localStorage.getItem('token');
    if (!token) return;

    // usar timer para ejecutar inmediatamente y luego cada intervalo
    this.sessionWatcherSub = timer(0, this.watchIntervalMs).pipe(
      switchMap(() => this.getByTokenSilent(token))
    ).subscribe({
      next: (s: any) => {
        // si la respuesta viene vacía/undefined/null o la sesión está marcada como no válida, forzar logout local
        if (!s || s.valid === false) {
          this.handleInvalidSession();
        } else {
          // actualizar valor local si es necesario
          this._session$.next(s);
        }
      },
      error: (err) => {
        // cualquier error al validar implica que la sesión ya no es válida
        console.warn('Session watcher detected invalid session', err);
        this.handleInvalidSession();
      }
    });
  }

  stopSessionWatcher() {
    if (this.sessionWatcherSub) {
      this.sessionWatcherSub.unsubscribe();
      this.sessionWatcherSub = null;
    }
    this.stopPresenceHeartbeat();
  }

  // Heartbeat to keep presence TTL refreshed while the client tab is open
  startPresenceHeartbeat() {
    if (this.presenceHeartbeatSub) return;

    const token = localStorage.getItem('token');
    if (!token) return;

    // call refreshActivity immediately and then every presenceHeartbeatMs
    this.presenceHeartbeatSub = timer(0, this.presenceHeartbeatMs).pipe(
      switchMap(() => this.refreshActivity())
    ).subscribe({
      next: () => {
        // noop
      },
      error: (e) => {
        console.warn('Presence heartbeat error', e);
        // if heartbeat fails, stop it to avoid noisy loops
        this.stopPresenceHeartbeat();
      }
    });
  }

  stopPresenceHeartbeat() {
    if (this.presenceHeartbeatSub) {
      this.presenceHeartbeatSub.unsubscribe();
      this.presenceHeartbeatSub = null;
    }
  }

  // Versión silenciosa de getByToken que no reinicia el watcher ni manipula el state
  private getByTokenSilent(token: string): Observable<any> {
    return this.http.get<any>(`${this.api}/token/${token}`);
  }

  // Manejar la invalidación de la sesión: limpiar estado, detener watcher y redirigir al login
  private handleInvalidSession() {
    try {
      localStorage.removeItem('token');
    } catch (e) { console.warn('Could not clear token', e); }
    this._session$.next(null);
    this.stopSessionWatcher();
    // navegar a la ruta de login (ajustar si la ruta es distinta)
    try {
      this.router.navigate(['/auth']);
    } catch (e) {
      console.warn('Router navigation to /auth failed', e);
      // fallback simple
      window.location.href = '/auth';
    }
  }
}
