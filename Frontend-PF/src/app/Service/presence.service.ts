import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class PresenceService {
    private http = inject(HttpClient);
    private api = '/app/v1/presence';

    getBulkPresence(userIds: string[]): Observable<Record<string, string>> {
        return this.http.post<Record<string, string>>(`${this.api}/realtime/bulk`, userIds);
    }

    getUserPresence(userId: string): Observable<string> {
        return this.http.get<string>(`${this.api}/realtime/${userId}`);
    }
}
