import { ChangeDetectorRef, Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../Service/session.service';
import { UserService } from '../../Service/user.service';
import { AuthService } from '../../Service/AuthService';
import { ConversationEventsService } from '../../Service/conversation-events.service';
import { finalize, take } from 'rxjs/operators';
import { Subject, Subscription } from 'rxjs';
import { UserAvailabilityService } from '../../Service/UserAvailabilityService';

@Component({
  selector: 'app-user-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-settings.html',
  styleUrls: ['./user-settings.scss']
})
export class UserSettings implements OnInit {
  // El bloque de perfil se deshabilitó en la vista de settings; dejamos la lógica intacta pero sin ejecutar.
  private readonly profileSectionEnabled = false;
  // Perfil del Usuario
  username = 'UsuarioPureChat';
  statusOptions = ['Online', 'Offline', 'Ausente', 'Ocupado'];
  selectedStatus = 'Online';
  profileImage: string | null = 'https://i.pravatar.cc/150?img=12';
  usernameTouched = false;
  private hasUserEditedUsername = false;
  // Archivo seleccionado para subir cuando se haga "Guardar Cambios"
  selectedPhotoFile: File | null = null;
  uploadingPhoto = false;
  // Validación de la foto
  readonly maxPhotoBytes = 10 * 1024 * 1024; // 10 MB
  readonly allowedPhotoTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  photoValidationMessage: string | null = null;
  photoValidationState: 'neutral' | 'ok' | 'error' = 'neutral';

  // Mensajes generales para mostrar al usuario (success/error/info)
  uploadMessage: string | null = null;
  uploadMessageType: 'success' | 'error' | 'info' | null = null;

  // Username availability (real-time)
  usernameAvailable: boolean | null = null;
  usernameRequired: boolean = false;
  usernameChecking = false;
  private usernameCheck$ = new Subject<string>();
  private subs = new Subscription();
  private sessionSub?: Subscription;

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
      // limpiar mensajes previos
      this.photoValidationMessage = null;
      this.photoValidationState = 'neutral';
      this.uploadMessage = null;
      this.uploadMessageType = null;

      if (file) {
        // validar tipo
        if (!this.allowedPhotoTypes.includes(file.type)) {
          this.photoValidationMessage = 'Formato no soportado. Usa JPG, PNG, GIF o WEBP.';
          this.photoValidationState = 'error';
          this.selectedPhotoFile = null;
          this.cdr.detectChanges();
          return;
        }

        // validar tamaño
        if (file.size > this.maxPhotoBytes) {
          this.photoValidationMessage = 'El archivo excede 10 MB. Selecciona un archivo más pequeño.';
          this.photoValidationState = 'error';
          this.selectedPhotoFile = null;
          this.cdr.detectChanges();
          return;
        }

        // Si pasa validaciones, previsualizar y marcar OK
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.profileImage = e.target.result;
          this.photoValidationMessage = 'La imagen cumple el tamaño máximo (10 MB).';
          this.photoValidationState = 'ok';
          this.selectedPhotoFile = file;
          this.cdr.detectChanges();
        };
        reader.readAsDataURL(file);
      }
    };
    input.click();
  }

  // Al inicializar el componente, cargar la sesión (si existe) y leer el usuario
  // para obtener `photoUrl` persistido en el backend.
  ngOnInit(): void {
    if (!this.profileSectionEnabled) {
      return; // perfil deshabilitado en settings, evitar llamadas y validaciones innecesarias
    }
    try {
      const cur = this.sessionService.currentSession;
      // If there's a cached session, populate username and profile image immediately
      if (cur) {
        this.applySessionData(cur);
      }

      // subscribe to session changes so the component updates when session is refreshed elsewhere
      this.sessionSub = this.sessionService.session$.subscribe((s: any) => {
        if (!s) return;
        this.applySessionData(s);
      });

      const token = localStorage.getItem('token');
      if (token) {
        // getByToken guarda la sesión en SessionService y la devuelve
        this.sessionService.getByToken(token).subscribe({
          next: (s: any) => {
            this.applySessionData(s);
            const uid = s?.userId;
            if (uid) {
              // only request full user entity if we don't already have username or photoUrl
              const haveName = !!(this.username && this.username !== 'UsuarioPureChat');
              const havePhoto = !!(this.profileImage && !this.profileImage.includes('pravatar'));
              if (!haveName || !havePhoto) {
                this.loadUserProfileImage(uid);
              }
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

    // subscribe to username availability checks (debounced/cached inside service)
    const sub = this.usernameCheck$.subscribe(name => {
      this.usernameChecking = true;
      this.userAvailability.checkUsernameAvailability(name).pipe(take(1), finalize(() => {
        this.usernameChecking = false;
        this.cdr.detectChanges();
      })).subscribe(av => {
        this.usernameAvailable = av;
        this.cdr.detectChanges();
      }, err => {
        this.usernameAvailable = false;
        this.cdr.detectChanges();
      });
    });
    this.subs.add(sub);
  }

  ngOnDestroy(): void {
    try { this.subs.unsubscribe(); } catch (e) { /* ignore */ }
    try { if (this.sessionSub) { this.sessionSub.unsubscribe(); } } catch (e) { /* ignore */ }
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
        // set real username from loaded user
        if (u?.username) {
          const canOverride = !this.hasUserEditedUsername || !this.username || this.username === 'UsuarioPureChat';
          if (canOverride) {
            this.username = u.username;
            this.usernameAvailable = null;
            this.usernameRequired = false;
          }
        }
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.warn('[UserSettings] error loading user by id', err);
      }
    });
  }

  onUsernameInput(value: string) {
    this.usernameTouched = true;
    this.hasUserEditedUsername = true;
    this.username = value || '';
    this.usernameAvailable = null;
    // mark required state
    this.usernameRequired = !this.username || this.username.trim().length === 0;
    if (this.usernameRequired) { this.cdr.detectChanges(); return; }
    this.usernameCheck$.next(this.username.trim());
  }

  // -----------------------
  // Seguridad / Sesiones
  // -----------------------
  private sessionService = inject(SessionService);
  private userService = inject(UserService);
  private userAvailability = inject(UserAvailabilityService);
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
      // mark required and show error message instead of alert
      this.usernameTouched = true;
      this.hasUserEditedUsername = true;
      this.usernameRequired = true;
      this.uploadMessage = 'El nombre de usuario es obligatorio.';
      this.uploadMessageType = 'error';
      this.cdr.detectChanges();
      return;
    } else {
      this.usernameRequired = false;
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
    
    if (!confirm('¿Guardar cambios realizados?')) {
      return;
    }

    // Prevent saving if username is known to be unavailable
    if (this.usernameAvailable === false) {
      const doNotifyAndMessage = (msg = 'Cambios guardados.', type: 'success' | 'error' | 'info' = 'success') => {
        try { this.convEvents.notifyRefresh(); } catch (e) { /* ignore */ }
        this.uploadMessage = msg;
        this.uploadMessageType = type;
        setTimeout(() => { this.uploadMessage = null; this.uploadMessageType = null; this.cdr.detectChanges(); }, 6000);
        this.cdr.detectChanges();
      };
      doNotifyAndMessage('El nombre de usuario no está disponible.', 'error');
      return;
    }

    // Si hay una foto seleccionada para subir, subirla primero y luego
    // refrescar la sesión y notificar a otros componentes.
    const session = this.sessionService.currentSession;
    const userId = session?.userId;

    const doNotifyAndMessage = (msg = 'Cambios guardados.', type: 'success' | 'error' | 'info' = 'success') => {
      try { this.convEvents.notifyRefresh(); } catch (e) { /* ignore */ }
      this.uploadMessage = msg;
      this.uploadMessageType = type;
      // auto-clear after a short time
      setTimeout(() => { this.uploadMessage = null; this.uploadMessageType = null; this.cdr.detectChanges(); }, 6000);
      this.cdr.detectChanges();
    };

    if (this.selectedPhotoFile) {
      const uid = userId;
      if (!uid) {
        doNotifyAndMessage('No se pudo determinar el usuario actual para subir la foto.', 'error');
        return;
      }

      this.uploadingPhoto = true;
      this.userService.uploadPhoto(uid, this.selectedPhotoFile)
        .pipe(take(1))
        .subscribe({
          next: (res: any) => {
            console.log('[UserSettings] upload response (saveChanges):', res);
            const newUrl = res?.photoUrl || res?.avatarUrl || res?.photo?.url;
            if (newUrl) {
              this.profileImage = newUrl;
            }

            // After upload, also update profile fields (username/status). Use returned fields if available
            const payload: any = { username: this.username, status: this.selectedStatus };
            if (res?.photoUrl) payload.photoUrl = res.photoUrl;
            if (res?.photoPath) payload.photoPath = res.photoPath;

            this.userService.updateProfile(uid, payload).pipe(take(1)).subscribe({
              next: (updated: any) => {
                // refresh session and notify
                const token = localStorage.getItem('token');
                if (token) {
                  this.sessionService.getByToken(token).pipe(take(1)).subscribe({
                    next: (s: any) => { doNotifyAndMessage('Cambios guardados. Foto subida.', 'success'); },
                    error: (e: any) => { console.warn('[UserSettings] session refresh failed', e); doNotifyAndMessage('Cambios guardados (error al refrescar sesión).', 'info'); }
                  });
                } else {
                  doNotifyAndMessage('Cambios guardados. Foto subida.', 'success');
                }
                this.selectedPhotoFile = null;
                this.uploadingPhoto = false;
                this.cdr.detectChanges();
              },
              error: (err: any) => {
                console.error('Error actualizando perfil', err);
                this.uploadingPhoto = false;
                const serverMsg = err?.error?.message || err?.message || 'Error actualizando perfil. Intenta nuevamente.';
                doNotifyAndMessage(serverMsg, 'error');
              }
            });
          },
          error: (err: any) => {
            console.error('Error subiendo la foto', err);
            this.uploadingPhoto = false;
            const serverMsg = err?.error?.message || err?.message || 'Error subiendo la foto. Intenta nuevamente.';
            doNotifyAndMessage(serverMsg, 'error');
          }
        });
      return;
    }

    // Si no hay foto nueva, persistir los campos del perfil (username/status)
    const uidNoPhoto = userId;
    if (!uidNoPhoto) {
      doNotifyAndMessage('No se pudo determinar el usuario actual.', 'error');
      return;
    }

    const payloadNoPhoto: any = { username: this.username, status: this.selectedStatus };
    // include current profileImage if present (not strictly necessary)
    if (this.profileImage) payloadNoPhoto.profileImage = this.profileImage;

    this.userService.updateProfile(uidNoPhoto, payloadNoPhoto).pipe(take(1)).subscribe({
      next: (updated: any) => {
        const token = localStorage.getItem('token');
        if (token) {
          this.sessionService.getByToken(token).pipe(take(1)).subscribe({
            next: (s: any) => { doNotifyAndMessage('Cambios guardados.', 'success'); },
            error: (e: any) => { console.warn('[UserSettings] session refresh failed', e); doNotifyAndMessage('Cambios guardados (error al refrescar sesión).', 'info'); }
          });
        } else {
          doNotifyAndMessage('Cambios guardados.', 'success');
        }
      },
      error: (err: any) => {
        console.error('Error actualizando perfil (sin foto)', err);
        const serverMsg = err?.error?.message || err?.message || 'Error actualizando perfil. Intenta nuevamente.';
        doNotifyAndMessage(serverMsg, 'error');
      }
    });
  }

  resetDefaults() {
    this.username = 'UsuarioPureChat';
    this.selectedStatus = 'Online';
    this.profileImage = 'https://i.pravatar.cc/150?img=12';
    this.usernameTouched = false;
    this.hasUserEditedUsername = false;
    this.usernameAvailable = null;
    this.usernameRequired = false;
    
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

  private applySessionData(session: any) {
    if (!session) return;
    const newUsername = session.username || session.displayName;
    const canOverride = !this.hasUserEditedUsername || !this.username || this.username === 'UsuarioPureChat';
    if (newUsername && canOverride) {
      this.username = newUsername;
      this.usernameRequired = false;
      // keep availability neutral so messages don't appear until the user interact
      this.usernameAvailable = null;
    }
    const newPhoto = session.photoUrl || session.profileImage;
    if (newPhoto) {
      this.profileImage = newPhoto;
    }
    this.cdr.detectChanges();
  }
}
