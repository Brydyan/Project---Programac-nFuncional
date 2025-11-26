import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { UserProfile } from '../Model/user-profile-model';
import { UserSettingsDto } from '../Model/user-settings-model';
export interface UserSearchResult {
  id: string;
  username: string;
  displayName: string;
  email: string;
  avatarUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  // En Docker/Nginx:
  private api = '/app/v1/user';


  constructor(private http: HttpClient) { }

  searchUsers(term: string, excludeId?: string): Observable<UserSearchResult[]> {
    let params = new HttpParams().set('q', term);
    if (excludeId) {
      params = params.set('excludeId', excludeId);
    }
    return this.http.get<UserSearchResult[]>(`${this.api}/search`, { params });
  }

  getUserById(id: string): Observable<UserSearchResult> {
    return this.http.get<UserSearchResult>(`${this.api}/${id}`);
  }

  getUsersByIds(ids: string[]): Observable<UserSearchResult[]> {
    if (ids.length === 0) {
      return of([]); // evita llamada HTTP innecesaria
    }
    const idsParam = ids.join(',');
    const params = new HttpParams().set('ids', idsParam);
    return this.http.get<UserSearchResult[]>(`${this.api}/by-ids`, { params });
  }

  getProfile(userId: string) {
    return this.http.get<UserProfile>(`${this.api}/profile/me`);
  }

  updateProfile(userId: string, profile: UserProfile) {
    return this.http.put<UserProfile>(`${this.api}/profile/me`, profile);
  }

  getSettings(userId: string) {
    return this.http.get<UserSettingsDto>(`${this.api}/settings/me`);
  }

  updateSettings(userId: string, settings: UserSettingsDto) {
    return this.http.put<UserSettingsDto>(`${this.api}/settings/me`, settings);
  }

  uploadPhoto(userId: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.api}/${userId}/photo-upload`, formData);
  }


}


