import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AvatarStorageService {
  /**
   * Mañana tu compa mete aquí Firebase Storage:
   * - sube el File
   * - devuelve la URL pública
   */
  async uploadAvatar(userId: string, file: File): Promise<string> {
    console.warn('[AvatarStorageService] TODO: implementar Firebase aquí');
    // de momento devolvemos string vacío para no romper nada
    return '';
  }
}
