import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, map } from 'rxjs/operators';

interface AvailabilityResponse {
  available: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserAvailabilityService {
  private readonly apiUrl = '/app/v1/user/available';
  private cache: Map<string, { timestamp: number; available: boolean }> = new Map();
  private readonly cacheDuration = 300000; // 5 minutes in ms

  constructor(private http: HttpClient) {}

  checkUsernameAvailability(username: string): Observable<boolean> {
    if (!username || username.trim().length === 0) {
      return of(false);
    }

    const cacheKey = `username:${username}`;
    const cached = this.getFromCache(cacheKey);
    if (cached !== null) {
      return of(cached);
    }

    return this.http.get<AvailabilityResponse>(`${this.apiUrl}/username/${username}`).pipe(
      map(response => response.available),
      catchError(() => of(false)), // On error, assume unavailable
      switchMap(available => {
        this.setCache(cacheKey, available);
        return of(available);
      })
    );
  }

  checkEmailAvailability(email: string): Observable<boolean> {
    if (!email || email.trim().length === 0) {
      return of(false);
    }

    const cacheKey = `email:${email}`;
    const cached = this.getFromCache(cacheKey);
    if (cached !== null) {
      return of(cached);
    }

    return this.http.get<AvailabilityResponse>(`${this.apiUrl}/email/${email}`).pipe(
      map(response => response.available),
      catchError(() => of(false)), // On error, assume unavailable
      switchMap(available => {
        this.setCache(cacheKey, available);
        return of(available);
      })
    );
  }

  private getFromCache(key: string): boolean | null {
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.cacheDuration) {
      return cached.available;
    }
    if (cached) {
      this.cache.delete(key); // Remove expired cache
    }
    return null;
  }

  private setCache(key: string, available: boolean): void {
    this.cache.set(key, { timestamp: Date.now(), available });
  }

  clearCache(): void {
    this.cache.clear();
  }
}
