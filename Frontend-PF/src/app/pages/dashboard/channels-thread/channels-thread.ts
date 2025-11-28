import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../../Service/session.service';
import { ChannelsMsgModel } from '../../../Model/channels-msg-model';
import { MemberModel } from '../../../Model/member-model';
import { Subscription, interval } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';
import { ChannelsMsgService } from '../../../Service/channels-msg.service';
import { ChannelsService } from '../../../Service/channels.service';
import { RealtimeService } from '../../../Service/realtime.service';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../../Service/user.service';
import { PresenceService } from '../../../Service/presence.service';
import { UserProfileEventsService } from '../../../Service/user-profile-events.service';

@Component({
  selector: 'app-channels-thread',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './channels-thread.html',
  styleUrl: './channels-thread.css',
})
export class ChannelsThread implements OnInit, OnDestroy {

  //WS para mensajes del canal
  private wsChannelSub?: Subscription;
  //WS para eventos de miembros (online/offline)
  private wsEventsSub?: Subscription;
  // Polling para presencia
  private presenceSub?: Subscription;
  // Eventos globales de actualización de perfil
  private profileEventsSub?: Subscription;

  private sessionService = inject(SessionService);
  private cdr = inject(ChangeDetectorRef);

  @Input() channelId!: string;
  @Input() channelName!: string;

  currentUserId!: string;

  messages: ChannelsMsgModel[] = [];
  newMessage = '';

  allUsers: MemberModel[] = [];
  membersInfo: MemberModel[] = [];
  onlineMembers: MemberModel[] = [];

  constructor(
    private channelMsgService: ChannelsMsgService,
    private channelService: ChannelsService,
    private realtime: RealtimeService,
    private route: ActivatedRoute,
    private userService: UserService,
    private presenceService: PresenceService,
    private profileEvents: UserProfileEventsService,
  ) {}

  ngOnInit(): void {
    // capturar id del canal desde la URL
    const idFromRoute = this.route.snapshot.paramMap.get('id');
    if (idFromRoute) this.channelId = idFromRoute;

    this.initSession();
    // reaccionar cuando alguien actualice su perfil (avatar/username)
    this.profileEventsSub = this.profileEvents.profileUpdate$.subscribe((userId) => {
      const member = this.membersInfo.find(m => m.userId === userId);
      if (member) {
        // refrescar datos para ese usuario
        this.userService.getUserById(userId).subscribe(u => {
          member.username = u.username || member.username;
          member.avatarUrl = u.photoUrl || u.avatarUrl || member.avatarUrl;
          this.cdr.detectChanges();
        });
      }
    });
  }

  ngOnDestroy(): void {
    this.wsChannelSub?.unsubscribe();
    this.wsEventsSub?.unsubscribe();
    this.presenceSub?.unsubscribe();
    this.profileEventsSub?.unsubscribe();
  }

  private initSession(): void {
    const token = localStorage.getItem('token');
    if (!token) return console.error('No token found');

    this.sessionService.getByToken(token).subscribe({
      next: (session) => {
        this.currentUserId = session.userId;

        // HTTP inicial
        this.loadMessages();
        this.loadAllUsers();

        // WS mensajes del canal
        this.subscribeChannelMessages();
      },
      error: (err) => console.error('Error obteniendo sesión del usuario', err),
    });
  }

  private loadMessages() {
    if (!this.channelId) return;

    this.channelMsgService.getByChannel(this.channelId).subscribe({
      next: (msgs) => {
        this.messages = msgs || [];
        setTimeout(() => this.scrollToBottom(), 50);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar mensajes del canal', err),
    });
  }

  private loadAllUsers() {
    // Primero obtenemos todos los canales
    this.channelService.getAll().subscribe(channels => {
      const channel = channels.find(c => c.id === this.channelId);
      if (!channel) return;

      // Asignar nombre del canal para mostrar en el header
      this.channelName = channel.name;

      // Crear miembros con placeholders para mostrar instantáneamente
      this.membersInfo = channel.members.map(id => ({
        id,
        userId: id,
        username: `Usuario ${id}`, // placeholder mientras llega el username real
        online: false,
        status: 'OFFLINE'
      }));

      this.updateOnlineMembers();

      // WS de eventos (online/offline)
      this.subscribeRealtimeEvents();
      // Iniciar polling de presencia cada 5 segundos (inmediatamente aunque no tengamos usernames)
      if (channel.members.length > 0) {
        this.presenceSub?.unsubscribe();
        this.presenceSub = interval(5000)
          .pipe(
            startWith(0),
            switchMap(() => this.presenceService.getBulkPresence(channel.members))
          )
          .subscribe((presenceMap: Record<string, any>) => {
            // Debug: mostrar el mapa recibido para verificar shape/keys
            console.debug('[Presence] bulk presence received:', presenceMap);
            this.membersInfo.forEach(m => {
              // intentar varias claves (userId o id) por compatibilidad
              const raw = presenceMap[m.userId] ?? presenceMap[m.id] ?? presenceMap[String(m.userId)] ?? null;
              const normalized = this.normalizePresenceValue(raw);
              m.status = normalized.status as any;
              m.online = normalized.online;
            });
            this.updateOnlineMembers();
            this.cdr.detectChanges();
          });

        // --- Llamada optimizada para obtener usernames reales ---
        this.userService.getUsersByIds(channel.members).subscribe(users => {
          // Reemplazar placeholders con los nombres reales y avatares (preservando status/online)
          this.membersInfo = this.membersInfo.map(m => {
            const u = users.find(user => user.id === m.userId);
            return {
              ...m,
              username: u?.username || m.username,
              avatarUrl: u?.avatarUrl || u?.photoUrl || m.avatarUrl
            };
          });
          this.cdr.detectChanges(); // actualizar la vista
        });
      }
    });
  }

  private updateOnlineMembers() {
    this.onlineMembers = this.membersInfo.filter((m) => m.status === 'ONLINE' || m.online);
    this.cdr.detectChanges();
  }

  // 🔔 WS: eventos tipo USER_STATUS_CHANGED
  private subscribeRealtimeEvents() {
    if (!this.currentUserId) return;

    this.wsEventsSub = this.realtime
      .subscribeToChannelEvents(this.currentUserId)
      .subscribe((event) => {
        if (event.type === 'USER_STATUS_CHANGED') {
          console.debug('[Presence] WS event:', event);
          const member = this.membersInfo.find(
            (m) => m.userId === event.payload.userId || m.id === event.payload.userId
          );
          if (member) {
            const toNormalize = event.payload.status ?? event.payload.online ?? event.payload;
            const normalized = this.normalizePresenceValue(toNormalize);
            member.status = normalized.status as any;
            member.online = normalized.online;
          }
          this.updateOnlineMembers();
        }
      });
  }

  // Normaliza distintos formatos de respuesta de presencia
  private normalizePresenceValue(raw: any): { status: 'ONLINE' | 'INACTIVE' | 'OFFLINE', online: boolean } {
    if (raw == null) return { status: 'OFFLINE', online: false };
    if (typeof raw === 'string') {
      const s = raw.trim().toUpperCase();
      return { status: (s === 'ONLINE' || s === 'INACTIVE') ? s as any : 'OFFLINE', online: s === 'ONLINE' };
    }
    if (typeof raw === 'boolean') {
      return { status: raw ? 'ONLINE' : 'OFFLINE', online: raw };
    }
    if (typeof raw === 'object') {
      if (raw.status) {
        const s = String(raw.status).trim().toUpperCase();
        return { status: (s === 'ONLINE' || s === 'INACTIVE') ? s as any : 'OFFLINE', online: s === 'ONLINE' };
      }
      if (typeof raw.online === 'boolean') {
        return { status: raw.online ? 'ONLINE' : 'OFFLINE', online: raw.online };
      }
      if (raw.state) {
        const s = String(raw.state).trim().toUpperCase();
        return { status: (s === 'ONLINE' || s === 'INACTIVE') ? s as any : 'OFFLINE', online: s === 'ONLINE' };
      }
    }
    return { status: 'OFFLINE', online: false };
  }

  // WS: mensajes del canal
  private subscribeChannelMessages() {
    if (!this.channelId) return;

    this.wsChannelSub = this.realtime
      .subscribeToChannelMessages(this.channelId)
      .subscribe((msg) => {
        // evitar duplicados si por alguna razón ya está
        const exists = this.messages.some(m => m.id === msg.id);
        if (!exists) {
          this.messages.push(msg);
          setTimeout(() => this.scrollToBottom(), 20);
          this.cdr.detectChanges();
        }
      });
  }

  sendMessage() {
    if (!this.newMessage.trim() || !this.currentUserId) return;

    const msg: Partial<ChannelsMsgModel> = {
      channel: this.channelId,
      senderId: this.currentUserId,
      messageContent: this.newMessage.trim(),
      status: 'SENT',
    };

    this.channelMsgService.create(msg).subscribe({
      next: () => {
        // El mensaje llegará por WebSocket desde el backend.
        this.newMessage = '';
      },
      error: (err) => console.error('Error al enviar mensaje', err),
    });
  }

  scrollToBottom() {
    const el = document.getElementById('channel-thread-messages');
    if (el) el.scrollTop = el.scrollHeight;
  }

  getSenderName(id: string): string {
    return id === this.currentUserId
      ? 'Tú'
      : this.membersInfo.find((u) => u.userId === id)?.username || id;
  }
}
