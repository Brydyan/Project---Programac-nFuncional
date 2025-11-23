import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ConversationService, ConversationSummary } from '../../../Service/conversation.service';

interface NewConversationUser {
  id: string;
  displayName: string;
  username: string;
  avatarUrl?: string;
}

@Component({
  selector: 'app-conversations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './conversations.html',
  styleUrls: ['./conversations.css']
})
export class Conversations implements OnInit {

  conversations: ConversationSummary[] = [];
  loading = true;
  error: string | null = null;
  currentUserId = 'user-demo-001';

  // UI "Nueva conversación"
  showNewConversation = false;
  searchTerm = '';
  searchResults: NewConversationUser[] = [];

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

  // Formatear hora/fecha del último mensaje
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
    this.router.navigate(['/dashboard/chat', conv.id]);
  }

  /* ─────────────────────────────
   *   NUEVA CONVERSACIÓN (UI)
   * ───────────────────────────── */

  newConversation() {
    this.showNewConversation = true;
    this.searchTerm = '';
    this.searchResults = [];
  }

  closeNewConversation() {
    this.showNewConversation = false;
  }

  onSearchChange() {
    const term = (this.searchTerm || '').trim().toLowerCase();

    if (!term) {
      this.searchResults = [];
      return;
    }

    // TODO: aquí luego llamaremos al backend (UserService /search)
    const mockUsers: NewConversationUser[] = [
      {
        id: 'user2',
        displayName: 'Alice Torres',
        username: 'alice',
        avatarUrl: 'https://i.pravatar.cc/100?img=5'
      },
      {
        id: 'user3',
        displayName: 'Equipo de Desarrollo',
        username: 'dev-team',
        avatarUrl: 'https://i.pravatar.cc/100?img=12'
      },
      {
        id: 'user4',
        displayName: 'Soporte Técnico',
        username: 'support',
        avatarUrl: 'https://i.pravatar.cc/100?img=32'
      }
    ];

    this.searchResults = mockUsers.filter(u =>
      u.displayName.toLowerCase().includes(term) ||
      u.username.toLowerCase().includes(term)
    );
  }

  startConversationWith(user: NewConversationUser) {
    // Más adelante aquí crearemos el Contact / Conversation en backend
    this.router.navigate(['/dashboard/chat', user.id]);
    this.showNewConversation = false;
  }
}
