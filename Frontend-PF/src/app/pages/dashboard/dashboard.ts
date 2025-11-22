// src/app/dashboard/dashboard.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../Service/session.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class Dashboard implements OnInit {

  searchText = '';
  activeSection = '';
  selectedConfigTab = 'perfil';

  // Datos usuario y configuración
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

  menuSections = [
    { title: 'Conversaciones', icon: '💬', route: '/dashboard/conversations' },
    { title: 'Canales',        icon: '📡', route: '/dashboard/channels' },
    { title: 'Configuración',  icon: '⚙️', route: '/dashboard/settings' },
    { title: 'Perfil',         icon: '👤', route: '/dashboard/profile' }
  ];

  constructor(private sessionService: SessionService, private router: Router) {}

  ngOnInit(): void {
    this.activeSection = '';
    // Refrescar sesión periodicamente
    setInterval(() => {
      this.sessionService.refreshActivity().subscribe();
    }, 30000);
  }

  navigateToSection(section: any) {
    this.activeSection = section.title;
    // Al entrar en configuracion selectTab por defecto
    if (this.activeSection === 'Configuración') {
      this.selectedConfigTab = 'perfil';
    }
  }

  selectConfigTab(tab: string) {
    this.selectedConfigTab = tab;
  }

  changePhoto() {
    alert('Función cambiar foto no implementada.');
  }

  saveChanges() {
    if (!this.username.trim()) {
      alert('El nombre de usuario es obligatorio.');
      this.selectedConfigTab = 'perfil';
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

  addFriend() {
    console.log('Añadir amigo');
  }

  viewNotifications() {
    console.log('Ver notificaciones');
  }

  getHelp() {
    console.log('Ayuda o soporte');
  }

  onSearch() {
    console.log('Buscando:', this.searchText);
  }

  logout() {
    console.log('Cerrando sesión...');

    const token = localStorage.getItem('token');
    if (!token) {
      localStorage.removeItem('token');
      this.router.navigate(['/auth']);
      return;
    }

    this.sessionService.getByToken(token).subscribe({
      next: (session: any) => {
        const sessionId = session?.sessionId || session?.id;
        if (sessionId) {
          this.sessionService.logout(sessionId).subscribe({
            next: () => {
              localStorage.removeItem('token');
              this.router.navigate(['/auth']);
            },
            error: () => {
              localStorage.removeItem('token');
              this.router.navigate(['/auth']);
            }
          });
        } else {
          localStorage.removeItem('token');
          this.router.navigate(['/auth']);
        }
      },
      error: () => {
        localStorage.removeItem('token');
        this.router.navigate(['/auth']);
      }
    });
  }
}