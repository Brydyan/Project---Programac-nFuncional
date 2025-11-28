import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { UserProfile } from '../Model/user-profile-model';

export interface UserSearchResult {
  id: string;
  username: string;
  displayName: string;
  email: string;
  avatarUrl?: string;
  photoUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  // En Docker/Nginx:
  private baseUrl = '/app/v1/user';


  constructor(private http: HttpClient) {}

  getProfile(userId: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.baseUrl}/${userId}`);
  }

  searchUsers(term: string, excludeId?: string): Observable<UserSearchResult[]> {
    let params = new HttpParams().set('q', term);
    if (excludeId) {
      params = params.set('excludeId', excludeId);
    }
    return this.http.get<UserSearchResult[]>(`${this.baseUrl}/search`, { params });
  }

  getUserById(id: string): Observable<UserSearchResult> {
    return this.http.get<UserSearchResult>(`${this.baseUrl}/${id}`);
  }

  getUsersByIds(ids: string[]): Observable<UserSearchResult[]> {
    if (ids.length === 0) {
      return of([]); // evita llamada HTTP innecesaria
    }
    const idsParam = ids.join(',');
    const params = new HttpParams().set('ids', idsParam);
    return this.http.get<UserSearchResult[]>(`${this.baseUrl}/by-ids`, { params });
  }

  // Subir foto de perfil al backend (multipart/form-data)
  uploadPhoto(userId: string, file: File) {
    const fd = new FormData();
    fd.append('file', file, file.name);
    return this.http.post<any>(`${this.baseUrl}/${userId}/photo-upload`, fd);
  }

  // Partial profile update (username, displayName, status, preferences, photoUrl/photoPath)
  updateProfile(userId: string, payload: Partial<UserProfile> & Record<string, any>) {
    return this.http.patch<UserProfile>(`${this.baseUrl}/${userId}/profile`, payload);
  }

}
