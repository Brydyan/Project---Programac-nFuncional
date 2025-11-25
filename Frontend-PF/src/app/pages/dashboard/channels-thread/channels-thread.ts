import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../../Service/session.service';
import { ChannelsMsgModel } from '../../../Model/channels-msg-model';
import { MemberModel } from '../../../Model/member-model';
import { Subscription } from 'rxjs';
import { ChannelsMsgService } from '../../../Service/channels-msg.service';
import { ChannelsService } from '../../../Service/channels.service';
import { RealtimeService } from '../../../Service/realtime.service';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../../Service/user.service';

@Component({
  selector: 'app-channels-thread',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './channels-thread.html',
  styleUrl: './channels-thread.css',
})
export class ChannelsThread implements OnInit, OnDestroy{
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

  private wsSub?: Subscription;

  constructor(
    private channelMsgService: ChannelsMsgService,
    private channelService: ChannelsService,
    private realtime: RealtimeService,
    private route: ActivatedRoute,
    private userService: UserService,
    
  ) {}

  ngOnInit(): void {
    // capturar id del canal desde la URL
    const idFromRoute = this.route.snapshot.paramMap.get('id');
    if (idFromRoute) this.channelId = idFromRoute;

    this.initSession();
  }


  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
  }

  private initSession(): void {
    const token = localStorage.getItem('token');
    if (!token) return console.error('No token found');

    this.sessionService.getByToken(token).subscribe({
      next: (session) => {
        this.currentUserId = session.userId;
        this.loadMessages();
        this.loadAllUsers();
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
        online: false
      }));

      this.updateOnlineMembers();
      this.subscribeRealtime();

      // --- Llamada optimizada para obtener usernames reales ---
      if (channel.members.length > 0) {
        this.userService.getUsersByIds(channel.members).subscribe(users => {
          // Reemplazar placeholders con los nombres reales
          this.membersInfo = this.membersInfo.map(m => {
            const u = users.find(user => user.id === m.userId);
            return {
              ...m,
              username: u?.username || m.username
            };
          });
          this.cdr.detectChanges(); // actualizar la vista
        });
      }
    });
  }


  private updateOnlineMembers() {
    this.onlineMembers = this.membersInfo.filter((m) => m.online);
    this.cdr.detectChanges();
  }

  private subscribeRealtime() {
    this.wsSub = this.realtime
      .subscribeToChannelEvents(this.currentUserId!)
      .subscribe((event) => {
        if (event.type === 'USER_STATUS_CHANGED') {
          const member = this.membersInfo.find(
            (m) => m.userId === event.payload.userId
          );
          if (member) member.online = event.payload.online;
          this.updateOnlineMembers();
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
      next: (createdMsg) => {
        this.messages.push(createdMsg);
        this.newMessage = '';
        setTimeout(() => this.scrollToBottom(), 20);
        this.cdr.detectChanges();
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
      // ✅ CORRECCIÓN 3: Si no encuentra el username, devuelve el ID.
      : this.membersInfo.find((u) => u.userId === id)?.username || id;
  }
}