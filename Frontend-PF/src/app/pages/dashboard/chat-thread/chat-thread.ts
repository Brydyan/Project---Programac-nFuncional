import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

interface ChatMessage{
id: string;
fromUserid: string;
toUserid: string;
content: string;
timestamp: string;
}

@Component({
  selector: 'app-chat-thread',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-thread.html',
  styleUrls: ['./chat-thread.css'],
})




export class ChatThread implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);

  contactId!: string;
  currentUserId = 'me'; 
  messages: ChatMessage[] = [];
  newMessage = '';
  loading = true;


  private sub: any;

   ngOnInit(): void {
    // Leer el contactId de la URL
    this.sub = this.route.paramMap.subscribe(params => {
      this.contactId = params.get('contactId') || '';
      this.loadMessages();
    });
  }

  ngOnDestroy(): void {
    if(this.sub) {
      this.sub.unsubscribe();
    }
  }


  loadMessages(): void {
    this.loading = true;
    //TODO se llama a un servicio para cargar los mensajes 
    
    setTimeout(()=>{
      this.messages= [
        {
          id: '1',
          fromUserid: this.contactId,
          toUserid: this.currentUserId,
          content: 'Hola, ¿cómo vas?',
          timestamp: new Date().toISOString()
        },
        {
          id: '2',
          fromUserid: this.currentUserId,
          toUserid: this.contactId,
          content: 'Todo bien, avanzando con PF 😎',
          timestamp: new Date().toISOString()
        }

      ];
      this.loading = false;
      this.scrollToBottom();
    }, 300)

  }


 sendMessage(): void {
    const text = this.newMessage.trim();
    if (!text) return;

    // Construir el mensaje nuevo
    const newMsg: ChatMessage = {
      id: crypto.randomUUID(),
      fromUserid: this.currentUserId,
      toUserid: this.contactId,
      content: text,
      timestamp: new Date().toISOString()
    };

    // Optimistic UI: pintarlo al tiro
    this.messages.push(newMsg);
    this.newMessage = '';
    this.scrollToBottom();

    // TODO: llamar a tu API / WebSocket para enviar el mensaje real
    // this.messageService.sendToUser(this.contactId, text).subscribe(...)
  }

  scrollToBottom(): void {
    // Lo implementas en el template con una referencia al contenedor
    setTimeout(() => {
      const el = document.getElementById('chat-thread-messages');
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    });
  }
}
