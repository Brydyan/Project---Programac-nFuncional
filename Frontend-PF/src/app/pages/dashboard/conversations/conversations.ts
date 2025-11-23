import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ConversationService, ConversationSummary } from '../../../Service/conversation.service';
import { UserService, UserSearchResult } from '../../../Service/user.service';


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
  searchResults: UserSearchResult[] = [];

  constructor(
    private router: Router,
    private convService: ConversationService,
    private userService: UserService
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

    const term = (this.searchTerm || '').trim();

    if (!term) {
      this.searchResults = [];
      return;
    }

    // opcional: mínimo 2 caracteres
    if (term.length < 2) {
      this.searchResults = [];
      return;
    }

   this.userService.searchUsers(term, this.currentUserId).subscribe({
      next: (users) => {
        this.searchResults = users;
      },
      error: (err) => {
        console.error('Error buscando usuarios', err);
        this.searchResults = [];
      }
    });

  }

   startConversationWith(user: UserSearchResult) {
    // Más adelante aquí crearemos el Contact / Conversation en backend
    this.router.navigate(['/dashboard/chat', user.id]);
    this.showNewConversation = false;
  }
}
