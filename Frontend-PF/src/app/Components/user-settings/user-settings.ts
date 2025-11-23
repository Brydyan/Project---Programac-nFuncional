import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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