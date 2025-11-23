import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

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
  private baseUrl = '/app/v1/user';

  // Si pruebas sin Docker, descomenta:
  // private baseUrl = 'http://localhost:8081/app/v1/user';

  constructor(private http: HttpClient) {}

  searchUsers(term: string, excludeId?: string): Observable<UserSearchResult[]> {
    let params = new HttpParams().set('q', term);
    if (excludeId) {
      params = params.set('excludeId', excludeId);
    }
    return this.http.get<UserSearchResult[]>(`${this.baseUrl}/search`, { params });
  }
}
