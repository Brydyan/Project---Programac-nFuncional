import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent } from '@angular/common/http';
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
  // optional attachment metadata
  attachmentUrl?: string;
  attachmentPath?: string;
  attachmentName?: string;
  attachmentMime?: string;
  attachmentSize?: number;
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

  sendDirect(senderId: string, receiverId: string, content: string, attachment?: {url?: string, path?: string, name?: string, contentType?: string, size?: number}): Observable<ChatMessage> {
    const payload: any = { senderId, receiverId, content };
    if (attachment) {
      payload.attachmentUrl = attachment.url;
      payload.attachmentPath = attachment.path;
      payload.attachmentName = attachment.name;
      payload.attachmentMime = attachment.contentType;
      payload.attachmentSize = attachment.size;
    }
    return this.http.post<ChatMessage>(`${this.baseUrl}/direct`, payload);
  }

  uploadAttachment(file: File, folder?: string): Observable<HttpEvent<any>> {
    const fd = new FormData();
    fd.append('file', file, file.name);
    if (folder) fd.append('folder', folder);
    return this.http.post<any>(`${this.baseUrl}/attachments`, fd, {
      reportProgress: true,
      observe: 'events'
    });
  }

  // Obtener la cantidad de mensajes no leídos en una conversación (por usuario)
  getUnreadCount(convId: string, userId: string) {
    return this.http.get<number>(`${this.baseUrl}/unread/conversation/${convId}/${userId}`);
  }

  // Obtener cantidad de conversaciones con pendientes para un usuario
  getPendingConversationsCount(userId: string) {
    return this.http.get<number>(`${this.baseUrl}/unread/user/${userId}`);
  }

  // Marcar conversación como leída (por el usuario)
  markConversationRead(convId: string, userId: string) {
    return this.http.post(`${this.baseUrl}/mark-read/${convId}/${userId}`, {});
  }
}
