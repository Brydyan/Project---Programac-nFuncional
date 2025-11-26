import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { SessionService } from '../../Service/session.service';
import { UserProfile } from '../../Model/user-profile-model';
import { UserService } from '../../Service/user.service';
import { FormsModule } from '@angular/forms';
import { UserProfileEventsService } from '../../Service/user-profile-events.service';

@Component({
  selector: 'app-userprofile',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './userprofile.html',
  styleUrls: ['./userprofile.scss']
})
export class UserProfileComponent implements OnInit {

  user: UserProfile | null = null;
  originalUser: UserProfile | null = null;

  loading = true;
  error: string | null = null;

  // flags de edición
  editingProfile = false;
  editingContact = false;

  // avatar local (preview)
  avatarPreview: string | null = null;
  selectedAvatarFile: File | null = null;

  constructor(
    private router: Router,
    private sessionService: SessionService,
    private userService: UserService,
    private cdr: ChangeDetectorRef,
    private profileEvents: UserProfileEventsService
  ) { }

  ngOnInit(): void {
    const token = localStorage.getItem('token');
    if (!token) {
      this.error = 'No se encontró token de sesión';
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    this.sessionService.getByToken(token).subscribe({
      next: (session) => {
        const userId = session.userId;

        this.userService.getProfile(userId).subscribe({
          next: (profile) => {
            this.user = profile;
            // copia para cancelar cambios
            this.originalUser = { ...profile };
            this.avatarPreview = profile.avatarUrl || null;

            this.loading = false;
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error('Error cargando perfil', err);
            this.error = 'No se pudo cargar el perfil';
            this.loading = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: (err) => {
        console.error('Error obteniendo sesión', err);
        this.error = 'No se pudo obtener la sesión';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ===== Navegación =====
  onCancel() {
    this.router.navigate(['/dashboard']);
  }

  onBackToChat() {
    this.router.navigate(['/dashboard']);
  }

  // ===== Edición =====

  toggleEditProfile() {
    if (this.editingProfile) {
      this.cancelEdits();
    } else {
      this.editingProfile = true;
      // Si abrimos perfil, cerramos contacto para evitar caos
      this.editingContact = false;
    }
  }

  toggleEditContact() {
    if (this.editingContact) {
      this.cancelEdits();
    } else {
      this.editingContact = true;
      // Si abrimos contacto, cerramos perfil
      this.editingProfile = false;
    }
  }

  onAvatarSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || !input.files.length) return;
    const file = input.files[0];
    this.selectedAvatarFile = file;

    const reader = new FileReader();
    reader.onload = () => {
      this.avatarPreview = reader.result as string;
      this.cdr.detectChanges();
    };
    reader.readAsDataURL(file);
  }

  cancelEdits() {
    if (this.originalUser) {
      this.user = { ...this.originalUser };
      this.avatarPreview = this.originalUser.avatarUrl || null;
    }
    this.editingProfile = false;
    this.editingContact = false;
    this.selectedAvatarFile = null;
    this.cdr.detectChanges();
  }

  saveProfile() {
    if (!this.user) return;

    const performUpdate = () => {
      const payload: UserProfile = {
        ...this.user!,
        avatarUrl: this.user!.avatarUrl || ''
      };

      this.userService.updateProfile(this.user!.id, payload).subscribe({
        next: (updated) => {
          this.user = updated;
          this.originalUser = { ...updated };
          this.avatarPreview = updated.avatarUrl || null;
          this.editingProfile = false;
          this.editingContact = false;
          this.selectedAvatarFile = null;

          // Notificar a otros componentes que el perfil fue actualizado
          this.profileEvents.notifyProfileUpdate(this.user!.id);

          this.cdr.detectChanges();
          alert('Perfil actualizado correctamente');
        },
        error: (err) => {
          console.error('Error actualizando perfil', err);
          alert('No se pudo actualizar el perfil');
        }
      });
    };

    if (this.selectedAvatarFile) {
      this.userService.uploadPhoto(this.user.id, this.selectedAvatarFile).subscribe({
        next: (updatedUser) => {
          // El backend retorna UserEntity con photoUrl
          if (updatedUser.photoUrl) {
            this.user!.avatarUrl = updatedUser.photoUrl;
          }
          performUpdate();
        },
        error: (err) => {
          console.error('Error uploading photo', err);
          alert('Error al subir la foto');
        }
      });
    } else {
      performUpdate();
    }
  }

  onEditPreferences() {
    this.router.navigate(['/dashboard/settings']);
  }

}
