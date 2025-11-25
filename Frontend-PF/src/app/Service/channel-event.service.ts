import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ChannelEventsService {
  private refreshSource = new Subject<void>();
  refresh$ = this.refreshSource.asObservable();

  emitRefresh() {
    this.refreshSource.next();
  }
}