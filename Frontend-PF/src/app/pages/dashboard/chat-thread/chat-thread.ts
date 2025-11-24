import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

import { SessionService } from '../../../Service/session.service';
import { HttpClient } from '@angular/common/http';
import { ChatMessage, MessageService } from '../../../Service/Message.service';
import { UserService } from '../../../Service/user.service';
import { RealtimeService } from '../../../Service/realtime.service';
import { Subscription } from 'rxjs';
import { ConversationEventsService } from '../../../Service/conversation-events.service';
@Component({
  selector: 'app-chat-thread',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-thread.html',
  styleUrls: ['./chat-thread.css'],
})
export class ChatThread implements OnInit, OnDestroy {
  private convEvents = inject(ConversationEventsService);
  private route = inject(ActivatedRoute);
  private sessionService = inject(SessionService);
  private messageService = inject(MessageService);
  private userService = inject(UserService);
  private realtimeService = inject(RealtimeService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  contactId!: string;
  currentUserId!: string;

  contactDisplayName: string | null = null;
  messages: ChatMessage[] = [];
  newMessage = '';
  loading = true;
  // Presencia del contacto
  contactPresence: string | null = null; // 'ONLINE' | 'INACTIVE' | 'OFFLINE' | null
  get contactStatusLabel(): string {
    if (this.contactPresence === 'ONLINE') return 'En línea';
    if (this.contactPresence === 'INACTIVE') return 'Inactivo';
    return 'Fuera de línea';
  }

  

  private routeSub?: Subscription;
  private realtimeSub?: Subscription;
  private presenceIntervalId: any = null;
  // Presencia
  private activityTimestamp = 0;
  private inactivityTimer: any = null;
  private activityListener = this.onUserActivity.bind(this);
  private beforeUnloadListener = this.onBeforeUnload.bind(this);

  ngOnInit(): void {
    // Nos suscribimos a los cambios de /chat/:id
    this.routeSub = this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (!id) return;

      // Reset de estado
      this.contactId = id;
      this.contactDisplayName = null;
      this.messages = [];
      this.loading = true;
      this.cdr.detectChanges();

      this.loadContact();
      this.initChat();
      // Registrar handlers globales de presencia (se añaden cuando el componente se crea)
      document.addEventListener('click', this.activityListener);
      document.addEventListener('keydown', this.activityListener);
      document.addEventListener('mousemove', this.activityListener);
      document.addEventListener('touchstart', this.activityListener);
      window.addEventListener('beforeunload', this.beforeUnloadListener);
    });
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.realtimeSub?.unsubscribe();
    this.stopPresencePolling();
    document.removeEventListener('click', this.activityListener);
    document.removeEventListener('keydown', this.activityListener);
    document.removeEventListener('mousemove', this.activityListener);
    document.removeEventListener('touchstart', this.activityListener);
    window.removeEventListener('beforeunload', this.beforeUnloadListener);
    this.stopInactivityTimer();
  }

  // =======================
  //   CARGA DE CONTACTO
  // =======================
  private loadContact(): void {
    if (!this.contactId) return;

    this.userService.getUserById(this.contactId).subscribe({
      next: (user) => {
        this.contactDisplayName = user.displayName || '@' + user.username;
        this.cdr.detectChanges();
      },
      error: () => {
        this.contactDisplayName = null;
        this.cdr.detectChanges();
      },
    });
  }

  // =======================
  //   INICIALIZAR CHAT
  // =======================
  private initChat(): void {
    const token = localStorage.getItem('token');
    if (!token) {
      this.loading = false;
      this.cdr.detectChanges();
      // iniciar polling de presencia para el contacto
      this.startPresencePolling();
      return;
    }

    this.sessionService.getByToken(token).subscribe({
      next: (session) => {
        this.currentUserId = session.userId;
        const sessionId = session.sessionId;

        // Marcar presencia online al entrar
        this.sessionService.markOnline(sessionId).subscribe({
          next: () => {
            // iniciar watcher de inactividad
            this.recordActivity();
            this.startInactivityTimer(sessionId);
            // start polling contact presence now that session is ready
            this.startPresencePolling();
          },
          error: (err) => console.error('markOnline error', err)
        });

        // 1) Cargar historial inicial
        this.loadMessages();

        // 2) Suscribirse a WebSocket para esta conversación
        const convId = this.buildConversationId(
          this.currentUserId,
          this.contactId
        );

        this.realtimeSub?.unsubscribe();
        this.realtimeSub = this.realtimeService
          .subscribeToDirect(convId)
          .subscribe((msg) => {
            // 1) Si el mensaje es mío, lo ignoro
            //    (ya lo agregué en sendMessage para que se vea instantáneo)
            if (msg.senderId === this.currentUserId) {
              return;
            }

            // 2) Evitar duplicados por si acaso
            if (this.messages.some((m) => m.id === msg.id)) {
              return;
            }

            this.messages = [...this.messages, msg];
            this.scrollToBottom();
            this.cdr.detectChanges();

            // 👇 avisar que hay cambios en conversaciones
            this.convEvents.notifyRefresh();
          });
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =======================
  // PRESENCE POLLING
  // =======================
  private startPresencePolling(): void {
    this.stopPresencePolling();
    // inmediatamente
    this.fetchContactPresence();
    // cada 15 segundos
    this.presenceIntervalId = setInterval(() => this.fetchContactPresence(), 15000);
  }

  private stopPresencePolling(): void {
    if (this.presenceIntervalId) {
      clearInterval(this.presenceIntervalId);
      this.presenceIntervalId = null;
    }
  }

  private fetchContactPresence(): void {
    if (!this.contactId) return;
    // endpoint devuelve texto: ONLINE | INACTIVE | OFFLINE
    (this.http.get(`/app/v1/presence/realtime/${this.contactId}`, { responseType: 'text' }) as any)
      .subscribe({
        next: (raw: string) => {
          let status = 'OFFLINE';
          if (!raw) {
            status = 'OFFLINE';
          } else {
            const text = raw.trim();
            // intentar parsear JSON si viene como objeto/array
            if ((text.startsWith('{') || text.startsWith('['))) {
              try {
                const parsed = JSON.parse(text);
                // si es array de sesiones, priorizar ONLINE > INACTIVE > OFFLINE
                if (Array.isArray(parsed)) {
                  const hasOnline = parsed.some((s: any) => (s.status || s) === 'ONLINE');
                  const hasInactive = parsed.some((s: any) => (s.status || s) === 'INACTIVE');
                  status = hasOnline ? 'ONLINE' : hasInactive ? 'INACTIVE' : 'OFFLINE';
                } else if (parsed && typeof parsed === 'object') {
                  status = parsed.status || parsed.presence || 'OFFLINE';
                } else {
                  status = String(parsed) as any || 'OFFLINE';
                }
              } catch (e) {
                status = text as any;
              }
            } else {
              status = text as any;
            }
          }

          // normalizar
          if (status !== 'ONLINE' && status !== 'INACTIVE') status = 'OFFLINE';
          this.contactPresence = status;
          this.cdr.detectChanges();
        },
        error: () => {
          this.contactPresence = 'OFFLINE';
          this.cdr.detectChanges();
        }
      });
  }

  // =======================
  // PRESENCE / ACTIVITY
  // =======================
  private onUserActivity(): void {
    const token = localStorage.getItem('token');
    if (!token) return;
    const session = this.sessionService.currentSession;
    if (!session || !session.sessionId) return;

    // cada interacción marca al usuario como ONLINE y reinicia el timer
    console.debug('[Presence] user activity detected, sessionId=', session.sessionId);
    this.sessionService.markOnline(session.sessionId).subscribe({
      next: () => {
        console.debug('[Presence] markOnline success for', session.sessionId);
        this.recordActivity();
      },
      error: (err) => console.error('markOnline onUserActivity', err)
    });
  }

  private recordActivity(): void {
    this.activityTimestamp = Date.now();
    this.stopInactivityTimer();
    // reiniciar timer
    const session = this.sessionService.currentSession;
    if (session && session.sessionId) this.startInactivityTimer(session.sessionId);
  }

  private startInactivityTimer(sessionId: string): void {
    // tiempo = 5 minutos
    this.stopInactivityTimer();
    this.inactivityTimer = setTimeout(() => {
      // no hubo actividad en 5 minutos -> marcar INACTIVE
      console.debug('[Presence] inactivity timer fired for', sessionId);
      this.sessionService.markInactive(sessionId).subscribe({
        next: () => {
          console.log('Sesión marcada como inactive por inactividad', sessionId);
        },
        error: (err) => console.error('markInactive error', err)
      });
    }, 5 * 60 * 1000);
  }

  // Debug helpers callable from browser console
  // Ejemplo: document.querySelector('app-chat-thread')?.__ngContext__[8].component.markPresenceOnlineForDebug()
  markPresenceOnlineForDebug(): void {
    const session = this.sessionService.currentSession;
    const sid = session?.sessionId;
    if (!sid) return console.warn('[Presence debug] no sessionId');
    console.debug('[Presence debug] forcing markOnline for', sid);
    this.sessionService.markOnline(sid).subscribe({ next: () => console.log('markOnline debug ok') , error: (e) => console.error(e) });
  }

  markPresenceInactiveForDebug(): void {
    const session = this.sessionService.currentSession;
    const sid = session?.sessionId;
    if (!sid) return console.warn('[Presence debug] no sessionId');
    console.debug('[Presence debug] forcing markInactive for', sid);
    this.sessionService.markInactive(sid).subscribe({ next: () => console.log('markInactive debug ok') , error: (e) => console.error(e) });
  }

  private stopInactivityTimer(): void {
    if (this.inactivityTimer) {
      clearTimeout(this.inactivityTimer);
      this.inactivityTimer = null;
    }
  }

  private onBeforeUnload(ev: BeforeUnloadEvent): void {
    const session = this.sessionService.currentSession;
    if (!session || !session.sessionId) return;

    // Intentar notificar logout para marcar como Fuera de linea
    try {
      // Al cerrar la pestaña, invalidamos la sesión (logout) para que el estado sea OFFLINE
      const token = localStorage.getItem('token');
      let url = `/app/v1/sessions/logout/${session.sessionId}`;
      if (token) {
        url += `?token=${encodeURIComponent(token)}`;
      }

      if (navigator && (navigator as any).sendBeacon) {
        try {
          const payload = token ? JSON.stringify({ token }) : '';
          (navigator as any).sendBeacon(url, payload);
        } catch (e) {
          // fallback síncrono
          const xhr = new XMLHttpRequest();
          xhr.open('POST', `/app/v1/sessions/logout/${session.sessionId}`, false);
          if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`);
          try { xhr.send(null); } catch (e) { /* ignore */ }
        }
      } else {
        const xhr = new XMLHttpRequest();
        xhr.open('POST', `/app/v1/sessions/logout/${session.sessionId}`, false);
        if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`);
        try { xhr.send(null); } catch (e) { /* ignore */ }
      }
    } catch (e) {
      // ignore
    }
  }

  private buildConversationId(a: string, b: string): string {
    return a < b ? `${a}_${b}` : `${b}_${a}`;
  }

  // =======================
  //   CARGAR MENSAJES
  // =======================
  private loadMessages(): void {
    if (!this.currentUserId || !this.contactId) {
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    this.loading = true;
    this.cdr.detectChanges();

    this.messageService
      .getDirectConversation(this.currentUserId, this.contactId)
      .subscribe({
        next: (msgs) => {
          this.messages = msgs;
          this.loading = false;
          this.scrollToBottom();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error cargando mensajes', err);
          this.loading = false;
          this.cdr.detectChanges();
        },
      });
  }

  // =======================
  //   ENVIAR MENSAJE
  // =======================
  sendMessage(): void {
    const text = this.newMessage.trim();
    if (!text || !this.currentUserId || !this.contactId) return;

    this.messageService
      .sendDirect(this.currentUserId, this.contactId, text)
      .subscribe({
        next: (msg) => {
          // limpiar input
          this.newMessage = '';

          // agregar al listado local
          this.messages = [...this.messages, msg];
          this.scrollToBottom();
          this.cdr.detectChanges();
          this.convEvents.notifyRefresh();
        },
        error: (err) => {
          console.error('Error enviando mensaje', err);
        },
      });
  }

  // =======================
  //   UTILIDADES DE VISTA
  // =======================
  isMine(msg: ChatMessage): boolean {
    return msg.senderId === this.currentUserId;
  }

  scrollToBottom(): void {
    setTimeout(() => {
      const el = document.getElementById('chat-thread-messages');
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    });
  }
}
