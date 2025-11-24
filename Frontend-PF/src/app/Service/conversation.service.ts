import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ConversationSummary {
  id: string;
  name: string;
  lastMessage: string;
  lastTime: string;     // viene del backend en ISO format
  avatarUrl: string;
  unreadCount: number;

  // propiedad calculada (no viene del backend)
  lastTimeLabel?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConversationService {

  // En Docker: nginx → backend pasa por /app/v1
  private baseUrl = '/app/v1/conversations';

  constructor(private http: HttpClient) {}

  getByUser(userId: string): Observable<ConversationSummary[]> {
    return this.http.get<ConversationSummary[]>(`${this.baseUrl}/${userId}`);
  }
}
