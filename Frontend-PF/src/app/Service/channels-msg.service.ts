import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ChannelsMsgModel } from '../Model/channels-msg-model';

@Injectable({
  providedIn: 'root'
})
export class ChannelsMsgService {
  private apiUrl = '/app/v1/channel-messages';

  constructor(private http: HttpClient) {}

  // Obtener mensajes por canal
  getByChannel(channelId: string): Observable<ChannelsMsgModel[]> {
    return this.http.get<ChannelsMsgModel[]>(
      `${this.apiUrl}/channel/${channelId}`
    );
  }

  // Crear mensaje
  create(msg: Partial<ChannelsMsgModel>): Observable<ChannelsMsgModel> {
    return this.http.post<ChannelsMsgModel>(this.apiUrl, msg);
  }

  // Obtener mensaje por ID
  getById(id: string): Observable<ChannelsMsgModel> {
    return this.http.get<ChannelsMsgModel>(`${this.apiUrl}/${id}`);
  }

  // Actualizar mensaje
  update(id: string, data: Partial<ChannelsMsgModel>): Observable<ChannelsMsgModel> {
    return this.http.put<ChannelsMsgModel>(`${this.apiUrl}/${id}`, data);
  }

  // Eliminar mensaje
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}