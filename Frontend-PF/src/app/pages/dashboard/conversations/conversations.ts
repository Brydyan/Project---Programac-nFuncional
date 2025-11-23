import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConversationService, ConversationSummary } from '../../../Service/conversation.service';

@Component({
  selector: 'app-conversations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './conversations.html',
  styleUrls: ['./conversations.css']
})
export class Conversations implements OnInit {

  conversations: ConversationSummary[] = [];
  loading = true;
  error: string | null = null;

  // TODO: luego lo obtendremos del JWT
  currentUserId = 'user-demo-001';

  constructor(
    private router: Router,
    private convService: ConversationService
  ) {}

  ngOnInit(): void {
    this.loadConversations();
  }

  loadConversations(): void {
    this.loading = true;

     this.convService.getByUser(this.currentUserId).subscribe({
    next: (data) => {
      // Convertir lastTime → lastTimeLabel
      this.conversations = data.map(conv => ({
        ...conv,
        lastTimeLabel: conv.lastTime
          ? this.formatTime(conv.lastTime)
          : ''
      }));

      this.loading = false;
    },
      error: (err) => {
        console.error('Error cargando conversaciones', err);
        this.error = 'No se pudieron cargar conversaciones';
        this.loading = false;
      }
    });
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

  // Si fue ayer
  const yesterday = new Date();
  yesterday.setDate(today.getDate() - 1);
  const isYesterday =
    date.getFullYear() === yesterday.getFullYear() &&
    date.getMonth() === yesterday.getMonth() &&
    date.getDate() === yesterday.getDate();

  if (isYesterday) return 'Ayer';

  // Si es hace días
  return date.toLocaleDateString('es-ES', {
    day: '2-digit',
    month: '2-digit'
  });
}

  openConversation(conv: ConversationSummary) {
    this.router.navigate(['/dashboard/chat', conv.id]);
  }

  newConversation() {
    console.log('Nueva conversación');
  }
}
