/* The Dashboard class in TypeScript represents a component for managing user interactions and
navigation within an Angular application. */
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../Service/session.service';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { ToastService } from '../../Shared/toast.service';   // ajusta la ruta si tu ToastService está en otra carpeta
import { UserService } from '../../Service/user.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterLink],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class Dashboard implements OnInit, OnDestroy {

  currentUserAlias: string | null = null;

  searchText = '';
  hasChildActive = false;

  menuSections = [
    { title: 'Conversaciones', icon: '💬', route: '/dashboard/conversations' },
    { title: 'Canales',        icon: '📡', route: '/dashboard/channels' },
    { title: 'Configuración',  icon: '⚙️', route: '/dashboard/settings' },
    { title: 'Perfil',         icon: '👤', route: '/dashboard/profile' }
  ];

  private activityIntervalId?: any;

  constructor(
    private sessionService: SessionService,
    private router: Router,
    private toast: ToastService,
    private userService: UserService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const token = localStorage.getItem('token');

    if (!token) {
      this.router.navigate(['/auth']);
      return;
    }

    this.sessionService.getByToken(token).subscribe({
      next: (session) => {
        console.log('[Dashboard] sesión OK', session);
        this.loadUserAliasAndStartRefresh(session);
      },
      error: () => {
        localStorage.removeItem('token');
        this.router.navigate(['/auth']);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.activityIntervalId) {
      clearInterval(this.activityIntervalId);
    }
  }

  /** Carga el alias del usuario y arranca el refresh periódico */
  private loadUserAliasAndStartRefresh(session: any) {
    const userId = session.userId;

    // 1) Pedir datos del usuario y sacar alias/username
    this.userService.getUserById(userId).subscribe({
      next: (user) => {
        console.log('[Dashboard] usuario OK', user);
        this.currentUserAlias =
          user.username || user.displayName || 'Usuario';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('[Dashboard] error getUserById', err);
        this.currentUserAlias = 'Usuario';
        this.cdr.detectChanges();
      }
    });

    // 2) Refrescar actividad cada 30 segundos
    this.activityIntervalId = setInterval(() => {
      this.sessionService.refreshActivity().subscribe();
    }, 30000);
  }

  // Estos dos se usan en el router-outlet del template
  onChildActivate() {
    this.hasChildActive = true;
  }

  onChildDeactivate() {
    this.hasChildActive = false;
  }

  addFriend() { console.log('Añadir amigo'); }
  viewNotifications() { console.log('Ver notificaciones'); }
  getHelp() { console.log('Ayuda o soporte'); }
  onSearch() { console.log('Buscando:', this.searchText); }

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
              this.toast.show('Tu sesión expiró. Inicia sesión de nuevo.', 'info');
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
