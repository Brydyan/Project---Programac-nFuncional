import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';

interface ConversationSummary {
  id: string;
  name: string;
  lastMessage: string;
  lastTimeLabel: string;
  avatarUrl: string;
}

@Component({
  selector: 'app-conversations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './conversations.html',
  styleUrls: ['./conversations.css']
})
export class Conversations {
  conversations: ConversationSummary[] = [
    {
      id: 'alice',
      name: 'Alicia Torres',
      lastMessage: 'Claro! Te envío los detalles por correo.',
      lastTimeLabel: 'Hace 5 min',
      avatarUrl: 'https://i.pravatar.cc/100?img=5'
    },
    // … más mocks
  ];

  constructor(private router: Router) {}

  openConversation(conv: ConversationSummary) {
    this.router.navigate(['/dashboard/chat', conv.id]);
  }

  newConversation() {
    console.log('Nueva conversación');
  }
}
