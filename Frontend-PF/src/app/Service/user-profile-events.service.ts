import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class UserProfileEventsService {
    private profileUpdateSubject = new Subject<string>();

    // Observable que otros componentes pueden suscribirse
    profileUpdate$ = this.profileUpdateSubject.asObservable();

    // Método para notificar que un perfil fue actualizado
    notifyProfileUpdate(userId: string) {
        this.profileUpdateSubject.next(userId);
    }
}
