import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class Dashboard {
  searchText = '';

  menuSections = [
    {
      title: 'Conversaciones',
      icon: '💬',
      route: '/dashboard/conversations'
    },
    {
      title: 'Canales',
      icon: '📡',
      route: '/dashboard/channels'
    },
    {
      title: 'Configuración',
      icon: '⚙️',
      route: '/dashboard/settings'
    },
    {
      title: 'Perfil',
      icon: '👤',
      route: '/dashboard/profile'
    }
  ];

  navigateToSection(section: any) {
    console.log('Navegando a:', section.route);
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
  }
}
