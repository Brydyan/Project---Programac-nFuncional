// src/app/Components/UserSettings/user-settings.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-user-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-settings.html',
  styleUrls: ['./user-settings.scss'],
})
export class UserSettings {
  username = 'UsuarioPureChat';
  statusOptions = ['Online', 'Offline', 'Ausente', 'Ocupado'];
  selectedStatus = 'Online';
  profileImage: string | null = null;

  notificationsSettings = {
    activate: false,
    sound: false,
    desktop: false
  };

  appearanceSettings = {
    theme: 'automatico',
    fontSize: 14
  };

  selectConfigTab(tab: string) {
    //this.selectedConfigTab = tab;
  }

  changePhoto() {
    alert('Función cambiar foto no implementada.');
  }

  saveChanges() {
    if (!this.username.trim()) {
      alert('El nombre de usuario es obligatorio.');
      //this.selectedConfigTab = 'perfil';
      return;
    }
    if (confirm('¿Guardar cambios realizados?')) {
      alert('Cambios guardados con éxito.');
    }
  }

  resetDefaults() {
    this.username = 'UsuarioPureChat';
    this.selectedStatus = 'Online';
    this.profileImage = null;
    this.notificationsSettings = {
      activate: false,
      sound: false,
      desktop: false
    };
    this.appearanceSettings = {
      theme: 'automatico',
      fontSize: 14
    };
    alert('Valores restablecidos a los predeterminados.');
  }
}