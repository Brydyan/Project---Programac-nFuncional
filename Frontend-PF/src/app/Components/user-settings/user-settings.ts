import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../Service/session.service';
import { UserService } from '../../Service/user.service';
import { AuthService } from '../../Service/AuthService';

@Component({
  selector: 'app-user-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-settings.html',
  styleUrls: ['./user-settings.scss']
})
export class UserSettings {
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
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.profileImage = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    };
    input.click();
  }

  // -----------------------
  // Seguridad / Sesiones
  // -----------------------
  private sessionService = inject(SessionService);
  private userService = inject(UserService);
  private authService = inject(AuthService);

  showSecurity = false;
  showPasswordModal = false;
  passwordConfirm = '';
  selectedSecurityTab: 'where' | 'current' = 'where';

  userIdForSessions: string | null = null;
  sessionsAll: any[] = [];
  sessionsCurrent: any[] = [];

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
  confirmPassword() {
    // obtener userId desde sesión actual
    const session = this.sessionService.currentSession;
    const userId = session?.userId;
    if (!userId) {
      alert('No se pudo obtener el usuario actual.');
      this.showPasswordModal = false;
      return;
    }
    // Verificar por userId para evitar creación de sesión accidental
    this.authService.verifyById({ userId, password: this.passwordConfirm }).subscribe(
      () => {
        // validado correctamente: cerrar modal y abrir seguridad (con pequeño retardo para evitar solapamiento)
        this.showPasswordModal = false;
        // limpiar la contraseña por seguridad
        this.passwordConfirm = '';
        this.userIdForSessions = userId;
        // esperar un tick para evitar que la ventana modal y el panel se solapen visualmente
        setTimeout(() => {
          this.showSecurity = true;
          this.loadSessions();
        }, 120);
      },
      (err: any) => {
        console.error('Password verification failed', err);
        alert('Contraseña incorrecta.');
      }
    );
  }

  // Cargar sesiones desde backend
  loadSessions() {
    if (!this.userIdForSessions) return;
    this.sessionService.getByUserId(this.userIdForSessions).subscribe(
      (list: unknown) => {
        const arr = (list as any[]) || [];
        this.sessionsAll = arr;
        this.sessionsCurrent = arr.filter((s: any) => s.valid === true);
      },
      (err) => { console.error('Error cargando sesiones', err); alert('Error cargando sesiones'); }
    );
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