import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ToastMessage {
  text: string;
  type?: 'info' | 'success' | 'error';
  id?: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private subject = new Subject<ToastMessage>();
  messages$ = this.subject.asObservable();

  show(text: string, type: 'info' | 'success' | 'error' = 'info') {
    const msg: ToastMessage = { text, type, id: Math.random().toString(36).slice(2) };
    this.subject.next(msg);
  }
}
