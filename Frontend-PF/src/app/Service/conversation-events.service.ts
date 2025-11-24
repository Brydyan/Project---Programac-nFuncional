import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ConversationEventsService {
  private refreshSubject = new Subject<void>();

  // Lo que escuchan otros componentes
  refresh$ = this.refreshSubject.asObservable();

  // Lo que llamamos desde el chat cuando hay cambios
  notifyRefresh() {
    this.refreshSubject.next();
  }
}
