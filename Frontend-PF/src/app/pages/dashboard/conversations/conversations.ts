import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLinkActive } from '@angular/router';
import { Subscription } from 'rxjs';

import {
  ConversationService,
  ConversationSummary
} from '../../../Service/conversation.service';
import { HttpClient } from '@angular/common/http';
import { UserService } from '../../../Service/user.service';
import { SessionService } from '../../../Service/session.service';
import { ConversationEventsService } from '../../../Service/conversation-events.service';
import { RealtimeService } from '../../../Service/realtime.service';
import { ChatMessage } from '../../../Service/Message.service';

@Component({
  selector: 'app-conversations',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLinkActive],
  templateUrl: './conversations.html',
  styleUrls: ['./conversations.css']
})
export class Conversations implements OnInit, OnDestroy {

  conversations: ConversationSummary[] = [];
  loading = true;              
  initialLoadDone = false;     
  error: string | null = null;
  currentUserId: string | null = null;

  showNewConversation = false;
  searchTerm = '';
  searchResults: any[] = [];

  private inboxSub?: Subscription;
  private eventsSub?: Subscription;

  private presenceIntervalId: any = null;

  selectedConversationId: string | null = null;

  constructor(
    private router: Router,
    private convService: ConversationService,
    private userService: UserService,
    private sessionService: SessionService,
    private convEvents: ConversationEventsService,
    private realtimeService: RealtimeService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const token = localStorage.getItem('token');
    if (!token) {
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    console.log('[Conversations] ngOnInit, pidiendo sesión...');

    this.sessionService.getByToken(token).subscribe({
      next: (session) => {
        console.log('[Conversations] sesión OK', session);
        this.currentUserId = session.userId;

        // 1) Cargar conversaciones iniciales (con loading visible)
        this.loadConversations(true);

        // 2) Refresco cuando ChatThread avisa (sin loading)
        this.eventsSub = this.convEvents.refresh$
          .subscribe(() => {
            console.log('[Conversations] refresh$ → recargar sin spinner');
            this.loadConversations(false);
          });

        // 3) Mensajes entrantes por WebSocket (sin recargar todo)
        this.inboxSub = this.realtimeService
          .subscribeToInbox(this.currentUserId!)
          .subscribe((msg: ChatMessage) => {
            console.log('[Conversations] nuevo mensaje en inbox', msg);
            this.applyIncomingMessage(msg);
          });
      },
      error: (err) => {
        console.error('[Conversations] error getByToken', err);
        this.error = 'No se pudo obtener la sesión';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy(): void {
    this.eventsSub?.unsubscribe();
    this.inboxSub?.unsubscribe();
    if (this.presenceIntervalId) {
      clearInterval(this.presenceIntervalId);
      this.presenceIntervalId = null;
    }

  }

  // showLoading = true solo en primera carga
  private loadConversations(showLoading: boolean): void {
  if (!this.currentUserId) return;

  if (showLoading && !this.initialLoadDone) {
    this.loading = true;
    this.cdr.detectChanges();
  }

  console.log('[Conversations] cargando conversaciones de', this.currentUserId);

  this.convService.getByUser(this.currentUserId).subscribe({
    next: (data: ConversationSummary[]) => {
      console.log('[Conversations] conversaciones recibidas', data);

      this.conversations = data.map(conv => ({
        ...conv,
        lastTimeLabel: conv.lastTime
          ? this.formatTime(conv.lastTime)
          : ''
      }));

      this.initialLoadDone = true;
      this.loading = false;
      this.cdr.detectChanges();

      // 🆕 refrescar presencia ahora…
      this.updatePresenceForAll();
      // 🆕 …y seguir refrescando cada cierto tiempo
      this.startPresenceAutoRefresh();
    },
    error: (err) => {
      console.error('[Conversations] error getByUser', err);
      this.error = 'No se pudieron cargar conversaciones';
      this.loading = false;
      this.cdr.detectChanges();
    }
  });
}

// 🔁 Llama al endpoint realtime para cada conversación
private updatePresenceForAll(): void {
  if (!this.conversations.length) return;

  this.conversations.forEach(conv => {
    this.http
      .get(`/app/v1/presence/realtime/${conv.id}`, { responseType: 'text' })
      .subscribe({
        next: (status) => {
          conv.presenceStatus = (status as string) || 'OFFLINE';
          this.cdr.detectChanges();
        },
        error: () => {
          conv.presenceStatus = 'OFFLINE';
          this.cdr.detectChanges();
        }
      });
  });
}

private startPresenceAutoRefresh(): void {
  // si ya había un timer, lo reiniciamos
  if (this.presenceIntervalId) {
    clearInterval(this.presenceIntervalId);
    this.presenceIntervalId = null;
  }

  this.presenceIntervalId = setInterval(() => {
    this.updatePresenceForAll();
  }, 5000);
}

  private applyIncomingMessage(msg: ChatMessage): void {
    if (!this.currentUserId) return;

    const otherId =
      msg.senderId === this.currentUserId ? msg.receiverId : msg.senderId;

    if (!otherId) return;

    const idx = this.conversations.findIndex(c => c.id === otherId);
    const lastTimeIso = msg.timestamp;
    const lastTimeLabel = this.formatTime(lastTimeIso);
    const isOpen = this.selectedConversationId === otherId;

    if (idx >= 0) {
      const conv = this.conversations[idx];
      const updated: ConversationSummary = {
        ...conv,
        lastMessage: msg.content,
        lastTime: lastTimeIso,
        lastTimeLabel,
        unreadCount: isOpen ? conv.unreadCount : conv.unreadCount + 1
      };

      const without = this.conversations.filter((_, i) => i !== idx);
      this.conversations = [updated, ...without];
    } else {
      const nueva: ConversationSummary = {
        id: otherId,
        name: 'Nuevo contacto',
        lastMessage: msg.content,
        lastTime: lastTimeIso,
        lastTimeLabel,
        avatarUrl: '',
        unreadCount: 1
      };
      this.conversations = [nueva, ...this.conversations];
    }

    // ya no tocamos loading aquí
    this.cdr.detectChanges();
  }

  formatTime(iso: string): string {
    const date = new Date(iso);

    const today = new Date();
    const isToday =
      date.getFullYear() === today.getFullYear() &&
      date.getMonth() === today.getMonth() &&
      date.getDate() === today.getDate();

    if (isToday) {
      return date.toLocaleTimeString('es-ES', {
        hour: '2-digit',
        minute: '2-digit'
      });
    }

    const yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);
    const isYesterday =
      date.getFullYear() === yesterday.getFullYear() &&
      date.getMonth() === yesterday.getMonth() &&
      date.getDate() === yesterday.getDate();

    if (isYesterday) return 'Ayer';

    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit'
    });
  }

  openConversation(conv: ConversationSummary) {
    this.selectedConversationId = conv.id;
    this.router.navigate(['/dashboard/chat', conv.id]);
  }

  // ========== NUEVA CONVERSACIÓN ==========

  newConversation() {
    this.showNewConversation = true;
    this.searchTerm = '';
    this.searchResults = [];
  }

  closeNewConversation() {
    this.showNewConversation = false;
  }

  onSearchChange() {
    const term = (this.searchTerm || '').trim();
    if (!term || term.length < 2) {
      this.searchResults = [];
      return;
    }

    this.userService.searchUsers(
      this.searchTerm,
      this.currentUserId ?? undefined
    ).subscribe({
      next: (users) => {
        this.searchResults = users;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('[Conversations] error searchUsers:', err);
        this.searchResults = [];
        this.cdr.detectChanges();
      }
    });
  }

  startConversationWith(user: any) {
    this.router.navigate(['/dashboard/chat', user.id]);
    this.showNewConversation = false;
  }
}
