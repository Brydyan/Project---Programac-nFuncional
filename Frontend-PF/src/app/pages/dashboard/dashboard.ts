import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../Service/session.service';
import { Router, RouterOutlet } from '@angular/router';
import { UserSettings } from '../../Components/user-settings/user-settings';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, UserSettings],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class Dashboard implements OnInit {
  searchText = '';
  activeSection = '';
  hasChildActive = false;   // para saber si hay un hijo activo (conversations, chat, etc.)

  menuSections = [
    { title: 'Conversaciones', icon: '💬', route: '/dashboard/conversations' },
    { title: 'Canales', icon: '📡', route: '/dashboard/channels' },
    { title: 'Configuración', icon: '⚙️', route: '/dashboard/settings' },
    { title: 'Perfil', icon: '👤', route: '/dashboard/profile' },
  ];

  constructor(
    private sessionService: SessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.activeSection = '';
    setInterval(() => {
      this.sessionService.refreshActivity().subscribe();
    }, 30000);
  }

  // Disparados por el router-outlet en el template
  onChildActivate() {
    this.hasChildActive = true;
  }

  onChildDeactivate() {
    this.hasChildActive = false;
  }

  navigateToSection(section: any) {
    // mantenemos el highlight de la sección + navegación real
    this.activeSection = section.title;
    this.router.navigateByUrl(section.route);
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

    // Busca la sesión por token y luego llama logout por sessionId
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
              // Aunque falle el logout del servidor, limpiamos el token local
              localStorage.removeItem('token');
              this.router.navigate(['/auth']);
            },
          });
        } else {
          localStorage.removeItem('token');
          this.router.navigate(['/auth']);
        }
      },
      error: () => {
        localStorage.removeItem('token');
        this.router.navigate(['/auth']);
      },
    });
  }
}
