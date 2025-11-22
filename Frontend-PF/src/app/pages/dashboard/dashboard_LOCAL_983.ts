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
  menuSections = [
    { title: 'Conversaciones', icon: '💬', route: '/dashboard/conversations' },
    { title: 'Canales',        icon: '📡', route: '/dashboard/channels' },
    { title: 'Configuración',  icon: '⚙️', route: '/dashboard/settings' },
    { title: 'Perfil',         icon: '👤', route: '/dashboard/profile' }
  ];

  constructor(private sessionService: SessionService, private router: Router) {}

  ngOnInit(): void {
    setInterval(() => {
      this.sessionService.refreshActivity().subscribe();
    }, 30000);
  }

  navigateToSection(section: any) { console.log('Navegando a:', section.route); }
  addFriend() { console.log('Añadir amigo'); }
  viewNotifications() { console.log('Ver notificaciones'); }
  getHelp() { console.log('Ayuda o soporte'); }
  onSearch() { console.log('Buscando:', this.searchText); }
  logout() {
    console.log('Cerrando sesión...');

    const token = localStorage.getItem('token');
    if (!token) {
      // No token stored — just clear and redirect
      localStorage.removeItem('token');
      this.router.navigate(['/auth']);
      return;
    }

    // Get session by token, then call logout by sessionId
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
              // Even if server logout fails, clear client token
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
