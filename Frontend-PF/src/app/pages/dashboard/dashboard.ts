import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../Service/session.service';
import { Router } from '@angular/router';
<<<<<<< HEAD
import { UserSettings } from '../../Components/user-settings/user-settings';
=======
import { RouterOutlet } from '@angular/router';   

>>>>>>> 03b419441a7f2e1b336a8c6dd5f791b148bdb771

@Component({
  selector: 'app-dashboard',
  standalone: true,
<<<<<<< HEAD
  imports: [CommonModule, FormsModule, UserSettings],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
=======
  imports: [CommonModule, FormsModule, RouterOutlet],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})

>>>>>>> 03b419441a7f2e1b336a8c6dd5f791b148bdb771
export class Dashboard implements OnInit {
  searchText = '';
<<<<<<< HEAD
  activeSection = '';

=======
  hasChildActive = false;   // 👈 NUEVO
>>>>>>> 03b419441a7f2e1b336a8c6dd5f791b148bdb771
  menuSections = [
    { title: 'Conversaciones', icon: '💬', route: '/dashboard/conversations' },
    { title: 'Canales', icon: '📡', route: '/dashboard/channels' },
    { title: 'Configuración', icon: '⚙️', route: '/dashboard/settings' },
    { title: 'Perfil', icon: '👤', route: '/dashboard/profile' },
  ];

  constructor(private sessionService: SessionService, private router: Router) {}

  




  ngOnInit(): void {
    this.activeSection = '';
    setInterval(() => {
      this.sessionService.refreshActivity().subscribe();
    }, 30000);
  }

<<<<<<< HEAD
  navigateToSection(section: any) {
    this.activeSection = section.title;
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
=======
  // 👇 NUEVOS MÉTODOS
  onChildActivate() {
    this.hasChildActive = true;
  }

  onChildDeactivate() {
    this.hasChildActive = false;
  }


 navigateToSection(section: any) {
  this.router.navigateByUrl(section.route);
}
  addFriend() { console.log('Añadir amigo'); }
  viewNotifications() { console.log('Ver notificaciones'); }
  getHelp() { console.log('Ayuda o soporte'); }
  onSearch() { console.log('Buscando:', this.searchText); }
  logout() {
    console.log('Cerrando sesión...');

    const token = localStorage.getItem('token');
    if (!token) {
      // No token stored — just clear and redirect
>>>>>>> 03b419441a7f2e1b336a8c6dd5f791b148bdb771
      localStorage.removeItem('token');
      this.router.navigate(['/auth']);
      return;
    }
<<<<<<< HEAD
=======

    // Get session by token, then call logout by sessionId
>>>>>>> 03b419441a7f2e1b336a8c6dd5f791b148bdb771
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
<<<<<<< HEAD
              localStorage.removeItem('token');
              this.router.navigate(['/auth']);
            },
=======
              // Even if server logout fails, clear client token
              localStorage.removeItem('token');
              this.router.navigate(['/auth']);
            }
>>>>>>> 03b419441a7f2e1b336a8c6dd5f791b148bdb771
          });
        } else {
          localStorage.removeItem('token');
          this.router.navigate(['/auth']);
        }
      },
      error: () => {
        localStorage.removeItem('token');
        this.router.navigate(['/auth']);
<<<<<<< HEAD
      },
    });
=======
      }
    });

>>>>>>> 03b419441a7f2e1b336a8c6dd5f791b148bdb771
  }
}