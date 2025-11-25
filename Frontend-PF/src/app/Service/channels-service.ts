import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ChannelsModel } from '../Model/channels-model';

@Injectable({
  providedIn: 'root',
})
export class ChannelsService {
  private apiUrl = '/app/v1/channels';

  constructor(private http: HttpClient) {}

  getAll(): Observable<ChannelsModel[]> {
    return this.http.get<ChannelsModel[]>(this.apiUrl);
  }
  
  getById(id: string): Observable<ChannelsModel> {
    return this.http.get<ChannelsModel>(`${this.apiUrl}/${id}`);
  }

  create(channel: Partial<ChannelsModel>): Observable<ChannelsModel> {
    return this.http.post<ChannelsModel>(this.apiUrl, channel);
  }

  update(id: string, channel: Partial<ChannelsModel>): Observable<ChannelsModel> {
    return this.http.put<ChannelsModel>(`${this.apiUrl}/${id}`, channel);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  joinChannel(channelId: string, userId: string) {
    return this.http.post<ChannelsModel>(`${this.apiUrl}/${channelId}/join/${userId}`, {});
  }

  searchPublic(name: string) {
    return this.http.get<ChannelsModel[]>(
      `${this.apiUrl}/public/search?name=${encodeURIComponent(name)}`
    );
  }

  getChannelsByMember(userId: string): Observable<ChannelsModel[]> {
    // Llama al nuevo endpoint del backend
    return this.http.get<ChannelsModel[]>(`${this.apiUrl}/member/${userId}`);
  }
}
