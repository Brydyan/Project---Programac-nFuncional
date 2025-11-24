import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

import { SessionService } from '../../../Service/session.service';
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
  private cdr = inject(ChangeDetectorRef);

  contactId!: string;
  currentUserId!: string;

  contactDisplayName: string | null = null;
  messages: ChatMessage[] = [];
  newMessage = '';
  loading = true;

  

  private routeSub?: Subscription;
  private realtimeSub?: Subscription;

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
    });
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.realtimeSub?.unsubscribe();
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
      return;
    }

    this.sessionService.getByToken(token).subscribe({
      next: (session) => {
        this.currentUserId = session.userId;

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
