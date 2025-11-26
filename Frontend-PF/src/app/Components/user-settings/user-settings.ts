import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../Service/session.service';
import { UserService } from '../../Service/user.service';
import { AuthService } from '../../Service/AuthService';
import { ConversationEventsService } from '../../Service/conversation-events.service';
import { finalize, take } from 'rxjs/operators';

@Component({
  selector: 'app-user-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-settings.html',
  styleUrls: ['./user-settings.scss']
})
export class UserSettings implements OnInit {
  // Perfil del Usuario
  username = 'UsuarioPureChat';
  statusOptions = ['Online', 'Offline', 'Ausente', 'Ocupado'];
  selectedStatus = 'Online';
  profileImage: string | null = 'https://i.pravatar.cc/150?img=12';

  // Notificaciones - Sección 1
  notificationsSettings = {
    activate: true,
    sound: true,
    desktop: false
  };

  // Apariencia
  appearanceSettings = {
    darkMode: false,
    fontSize: 16
  };

  // Notificaciones - Sección 2
  notificationPreferences = {
    email: true,
    push: true,
    inApp: true
  };

  // Idioma y Zona Horaria
  languageSettings = {
    interfaceLanguage: 'español',
    timezone: 'Europe/Madrid (GMT+1)'
  };

  languageOptions = [
    'español',
    'english',
    'français',
    'deutsch',
    'italiano'
  ];

  timezoneOptions = [
    'Europe/Madrid (GMT+1)',
    'UTC (GMT+0)',
    'America/New_York (GMT-5)',
    'America/Mexico_City (GMT-6)',
    'Asia/Tokyo (GMT+9)'
  ];

  changePhoto() {
    // Simular selección de archivo
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = (event: any) => {
      const file: File = event.target.files[0];
      if (file) {
        // mostrar preview inmediato
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.profileImage = e.target.result;
          this.cdr.detectChanges();
        };
        reader.readAsDataURL(file);

        // subir al backend
        const session = this.sessionService.currentSession;
        const userId = session?.userId;
        if (!userId) {
          alert('No se pudo determinar el usuario actual.');
          return;
        }

        // mostrar indicador simple
        const prev = (document.activeElement as HTMLElement);

        this.userService.uploadPhoto(userId, file).subscribe({
          next: (res: any) => {
            // debug: ver respuesta del backend
            console.log('[UserSettings] upload response:', res);
            // backend devuelve el usuario actualizado (photoUrl o photoPath)
            const newUrl = res?.photoUrl || res?.avatarUrl || res?.photo?.url;
            if (newUrl) {
              this.profileImage = newUrl;
            }

            // Refrescar sesión para propagar la nueva foto en toda la app
            const token = localStorage.getItem('token');
            if (token) {
              this.sessionService.getByToken(token).subscribe({
                next: (s: any) => {
                  console.log('[UserSettings] session refreshed:', s);
                  // luego de refrescar la sesión, notificar a componentes que recarguen
                  this.convEvents.notifyRefresh();
                },
                error: (e: any) => {
                  console.warn('[UserSettings] session refresh failed', e);
                  // aun si falla la recarga de sesión, intentar notificar para forzar recarga
                  this.convEvents.notifyRefresh();
                }
              });
            } else {
              // si no hay token, igual notificar para forzar recarga
              this.convEvents.notifyRefresh();
            }

            alert('Foto actualizada correctamente.');
            this.cdr.detectChanges();
          },
          error: (err: any) => {
            console.error('Error subiendo la foto', err);
            alert('Error subiendo la foto. Intenta nuevamente.');
          }
        });
      }
    };
    input.click();
  }

  // Al inicializar el componente, cargar la sesión (si existe) y leer el usuario
  // para obtener `photoUrl` persistido en el backend.
  ngOnInit(): void {
    try {
      const cur = this.sessionService.currentSession;
      if (cur && cur.userId) {
        // ya tenemos sesión en memoria: pedir datos del usuario
        this.loadUserProfileImage(cur.userId);
        return;
      }

      const token = localStorage.getItem('token');
      if (token) {
        // getByToken guarda la sesión en SessionService y la devuelve
        this.sessionService.getByToken(token).subscribe({
          next: (s: any) => {
            const uid = s?.userId;
            if (uid) {
              this.loadUserProfileImage(uid);
            }
          },
          error: (e: any) => {
            console.warn('[UserSettings] could not refresh session on init', e);
          }
        });
      }
    } catch (e) {
      console.warn('[UserSettings] ngOnInit error', e);
    }
  }

  private loadUserProfileImage(userId: string) {
    if (!userId) return;
    this.userService.getUserById(userId).subscribe({
      next: (u: any) => {
        // el backend puede devolver `photoUrl`, o `avatarUrl`, o estructura anidada
        const newUrl = u?.photoUrl || u?.avatarUrl || u?.photo?.url || u?.profileImage || u?.avatarUrl;
        if (newUrl) {
          console.log('[UserSettings] loaded user photo:', newUrl);
          this.profileImage = newUrl;
          this.cdr.detectChanges();
        }
      },
      error: (err: any) => {
        console.warn('[UserSettings] error loading user by id', err);
      }
    });
  }

  // -----------------------
  // Seguridad / Sesiones
  // -----------------------
  private sessionService = inject(SessionService);
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private convEvents = inject(ConversationEventsService);
  private cdr = inject(ChangeDetectorRef);

  showSecurity = false;
  showPasswordModal = false;
  passwordConfirm = '';
  isVerifyingPassword = false;
  selectedSecurityTab: 'where' | 'current' = 'where';

  userIdForSessions: string | null = null;
  sessionsAll: any[] = [];
  sessionsCurrent: any[] = [];
  sessionsLoading = false;

  // Abrir el panel de Seguridad - solicita contraseña
  openSecurity(tab: 'where' | 'current' = 'where') {
    this.selectedSecurityTab = tab;
    // Si ya hemos verificado, mostrar directamente
    if (this.showSecurity) return;

    // Pedir contraseña
    this.showPasswordModal = true;
    this.passwordConfirm = '';
  }

  // Verificar contraseña (usa el endpoint /auth/login para validar)
  confirmPassword(event?: Event) {
    event?.preventDefault();
    if (this.isVerifyingPassword) return;

    const password = this.passwordConfirm.trim();
    if (!password) {
      alert('Ingresa tu contraseña.');
      return;
    }

    this.isVerifyingPassword = true;
    // obtener userId desde sesión actual
    const session = this.sessionService.currentSession;
    const userId = session?.userId;
    if (!userId) {
      alert('No se pudo obtener el usuario actual.');
      this.showPasswordModal = false;
      this.isVerifyingPassword = false;
      return;
    }
    // Verificar por userId para evitar creación de sesión accidental
    this.authService.verifyById({ userId, password })
      .pipe(
        take(1),
        finalize(() => { this.isVerifyingPassword = false; })
      )
      .subscribe({
        next: () => {
          // validado correctamente: cerrar modal y abrir seguridad (con pequeño retardo para evitar solapamiento)
          this.showPasswordModal = false;
          this.cdr.detectChanges(); // fuerza cierre inmediato del modal
          // limpiar la contraseña por seguridad
          this.passwordConfirm = '';
          this.userIdForSessions = userId;
          // esperar un tick para evitar que la ventana modal y el panel se solapen visualmente
          setTimeout(() => {
            this.showSecurity = true;
            this.loadSessions();
            this.cdr.detectChanges();
          }, 120);
        },
        error: (err: any) => {
          console.error('Password verification failed', err);
          alert('Contraseña incorrecta.');
          this.cdr.detectChanges();
        }
      });
  }

  // Cargar sesiones desde backend
  loadSessions() {
    if (!this.userIdForSessions) return;
    this.sessionsLoading = true;
    this.sessionService.getByUserId(this.userIdForSessions)
      .pipe(finalize(() => {
        this.sessionsLoading = false;
        this.cdr.detectChanges();
      }))
      .subscribe(
        (list: unknown) => {
          const arr = (list as any[]) || [];
          this.sessionsAll = arr;
          this.sessionsCurrent = arr.filter((s: any) => s.valid === true);
        },
        (err) => { console.error('Error cargando sesiones', err); alert('Error cargando sesiones'); }
      );
  }

  // Cerrar una sesión específica
  logoutSession(sessionId: string) {
    if (!sessionId) return;
    if (!confirm('¿Cerrar esta sesión?')) return;
    this.sessionService.logout(sessionId).subscribe({
      next: () => {
        // recargar la lista desde el servidor para mantener consistencia
        this.loadSessions();
        alert('Sesión cerrada');
      },
      error: (e) => { console.error('Error cerrando sesión', e); alert('No se pudo cerrar la sesión'); }
    });
  }

  // Cerrar todas las sesiones del usuario
  logoutAllSessions() {
    if (!this.userIdForSessions) return;
    if (!confirm('¿Cerrar todas las sesiones de este usuario?')) return;
    this.sessionService.logoutAll(this.userIdForSessions).subscribe({
      next: () => {
        // recargar para mostrar el estado actual (probablemente vacío)
        this.loadSessions();
        alert('Todas las sesiones han sido cerradas');
      },
      error: (e) => { console.error('Error cerrando todas las sesiones', e); alert('No fue posible cerrar todas las sesiones'); }
    });
  }

  saveChanges() {
    if (!this.username.trim()) {
      alert('El nombre de usuario es obligatorio.');
      return;
    }
    
    // Simular guardado de configuración
    const settings = {
      profile: {
        username: this.username,
        status: this.selectedStatus,
        profileImage: this.profileImage
      },
      notifications: this.notificationsSettings,
      appearance: this.appearanceSettings,
      preferences: this.notificationPreferences,
      language: this.languageSettings
    };

    console.log('Guardando configuración:', settings);
    
    if (confirm('¿Guardar cambios realizados?')) {
      alert('Cambios guardados con éxito.');
    }
  }

  resetDefaults() {
    this.username = 'UsuarioPureChat';
    this.selectedStatus = 'Online';
    this.profileImage = 'https://i.pravatar.cc/150?img=12';
    
    this.notificationsSettings = {
      activate: true,
      sound: true,
      desktop: false
    };
    
    this.appearanceSettings = {
      darkMode: false,
      fontSize: 16
    };
    
    this.notificationPreferences = {
      email: true,
      push: true,
      inApp: true
    };
    
    this.languageSettings = {
      interfaceLanguage: 'español',
      timezone: 'Europe/Madrid (GMT+1)'
    };
    
    alert('Valores restablecidos a los predeterminados.');
  }
}
