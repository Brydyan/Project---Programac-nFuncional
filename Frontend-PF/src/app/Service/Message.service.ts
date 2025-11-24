import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  id: string;
  senderId: string;
  receiverId: string | null;
  channelId?: string | null;
  content: string;
  timestamp: string;
  edited: boolean;
  deleted: boolean;
}

@Injectable({ providedIn: 'root' })
export class MessageService {

  private baseUrl = '/app/v1/messages';

  constructor(private http: HttpClient) {}

  getDirectConversation(currentUserId: string, contactId: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(
      `${this.baseUrl}/direct/${currentUserId}/${contactId}`
    );
  }

  sendDirect(senderId: string, receiverId: string, content: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.baseUrl}/direct`, {
      senderId,
      receiverId,
      content
    });
  }
}
