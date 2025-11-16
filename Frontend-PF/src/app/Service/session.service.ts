import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SessionService {

  private api = 'http://localhost:8081/app/v1/sessions';

  constructor(private http: HttpClient) {}

  refreshActivity(): Observable<any> {
    const token = localStorage.getItem('token');
    if (!token) return new Observable();

    return this.http.post(`${this.api}/refresh/${token}`, {});
  }
}
