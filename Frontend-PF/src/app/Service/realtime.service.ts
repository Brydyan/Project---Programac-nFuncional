import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Observable, ReplaySubject } from 'rxjs';
import { filter, switchMap } from 'rxjs/operators';
import { ChatMessage } from './Message.service';

@Injectable({ providedIn: 'root' })
export class RealtimeService {
  private client: Client;
  private connected$ = new ReplaySubject<boolean>(1);

  constructor() {
    this.client = new Client({
    brokerURL: `ws://${window.location.host}/ws`,
    reconnectDelay: 5000,
    debug: (str) => console.log('[STOMP]', str),
  });

    this.client.onConnect = () => {
      console.log('[STOMP] conectado');
      this.connected$.next(true);
    };

    this.client.onStompError = (frame) => {
      console.error('[STOMP] error', frame);
    };

    this.client.activate();
  }

  private waitUntilConnected(): Observable<boolean> {
    return this.connected$.pipe(filter((v) => v === true));
  }

  subscribeToDirect(convId: string): Observable<ChatMessage> {
    return this.waitUntilConnected().pipe(
      switchMap(
        () =>
          new Observable<ChatMessage>((observer) => {
            const sub = this.client.subscribe(
              `/topic/direct.${convId}`,
              (msg: IMessage) => {
                const body = JSON.parse(msg.body) as ChatMessage;
                observer.next(body);
              }
            );

            return () => sub.unsubscribe();
          })
      )
    );
  }
  
/**  Inbox global del usuario (para la lista de conversaciones) */
  subscribeToInbox(userId: string): Observable<ChatMessage> {
    return this.waitUntilConnected().pipe(
      switchMap(
        () =>
          new Observable<ChatMessage>((observer) => {
            const sub = this.client.subscribe(
              `/topic/inbox.${userId}`,
              (msg: IMessage) => {
                const body = JSON.parse(msg.body) as ChatMessage;
                observer.next(body);
              }
            );

            return () => sub.unsubscribe();
          })
      )
    );
  }
  
  subscribeToChannelEvents(userId: string): Observable<any> {
    return this.waitUntilConnected().pipe(
      switchMap(
        () =>
          new Observable<any>((observer) => {
            const sub = this.client.subscribe(
              `/topic/channels.${userId}`,
              (msg: IMessage) => {
                console.log('[WS] evento canal recibido:', msg.body);
                observer.next(JSON.parse(msg.body));
              }
            );

            return () => sub.unsubscribe();
          })
      )
    );
  }
}
