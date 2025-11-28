import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';
import { take } from 'rxjs/operators';
import { SessionService } from '../../Service/session.service';
import { UserService } from '../../Service/user.service';
import { UserProfile } from '../../Model/user-profile-model';
import { UserAvailabilityService } from '../../Service/UserAvailabilityService';

@Component({
  selector: 'app-userprofile',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './userprofile.html',
  styleUrls: ['./userprofile.scss']
})
export class UserProfileComponent implements OnInit, OnDestroy {

  user: UserProfile | null = null;
  originalUser: UserProfile | null = null;

  loading = true;
  uploadingAvatar = false;
  error: string | null = null;

  // Validación de usuario
  usernameTouched = false;
  usernameAvailable: boolean | null = null;
  usernameRequired = false;
  usernameChecking = false;
  private usernameCheck$ = new Subject<string>();
  private subs = new Subscription();

  // flags de edición
  editingProfile = false;
  editingContact = false;

  // avatar local (preview)
  avatarPreview: string | null = null;
  selectedAvatarFile: File | null = null;
  readonly maxPhotoBytes = 10 * 1024 * 1024; // 10 MB
  readonly allowedPhotoTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  photoValidationMessage: string | null = null;
  photoValidationState: 'neutral' | 'ok' | 'error' = 'neutral';

  constructor(
    private router: Router,
    private sessionService: SessionService,
    private userService: UserService,
    private userAvailability: UserAvailabilityService,
    private cdr: ChangeDetectorRef
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
            this.avatarPreview = profile.avatarUrl || profile.photoUrl || null;

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

    const sub = this.usernameCheck$.subscribe(name => {
      this.usernameChecking = true;
      this.userAvailability.checkUsernameAvailability(name).pipe(take(1)).subscribe({
        next: (av) => {
          this.usernameAvailable = av;
          this.usernameChecking = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.usernameAvailable = false;
          this.usernameChecking = false;
          this.cdr.detectChanges();
        }
      });
    });
    this.subs.add(sub);
  }

  ngOnDestroy(): void {
    try { this.subs.unsubscribe(); } catch (e) { /* ignore */ }
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
      this.usernameTouched = false;
      this.usernameRequired = false;
      this.usernameAvailable = null;
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
    this.photoValidationMessage = null;
    this.photoValidationState = 'neutral';

    if (!file) return;
    // validar tipo
    if (!this.allowedPhotoTypes.includes(file.type)) {
      this.photoValidationMessage = 'Formato no soportado. Usa JPG, PNG, GIF o WEBP.';
      this.photoValidationState = 'error';
      this.selectedAvatarFile = null;
      this.cdr.detectChanges();
      return;
    }

    // validar tamaño
    if (file.size > this.maxPhotoBytes) {
      this.photoValidationMessage = 'El archivo excede 10 MB. Selecciona un archivo más pequeño.';
      this.photoValidationState = 'error';
      this.selectedAvatarFile = null;
      this.cdr.detectChanges();
      return;
    }

    this.selectedAvatarFile = file;

    const reader = new FileReader();
    reader.onload = () => {
      this.avatarPreview = reader.result as string;
      this.photoValidationMessage = 'La imagen cumple el tamaño máximo (10 MB).';
      this.photoValidationState = 'ok';
      this.cdr.detectChanges();
    };
    reader.readAsDataURL(file);
  }

  cancelEdits() {
    if (this.originalUser) {
      this.user = { ...this.originalUser };
      this.avatarPreview = this.originalUser.avatarUrl || this.originalUser.photoUrl || null;
    }
    this.editingProfile = false;
    this.editingContact = false;
    this.selectedAvatarFile = null;
    this.photoValidationMessage = null;
    this.photoValidationState = 'neutral';
    this.usernameTouched = false;
    this.usernameRequired = false;
    this.usernameAvailable = null;
    this.cdr.detectChanges();
  }

  saveProfile() {
    if (!this.user) return;

    const usernameValue = (this.user.username || '').trim();
    if (!usernameValue) {
      this.usernameTouched = true;
      this.usernameRequired = true;
      this.cdr.detectChanges();
      return;
    }
    if (this.usernameAvailable === false) {
      alert('El nombre de usuario ya está en uso. Elige otro.');
      return;
    }
    this.user.username = usernameValue;

    const applyProfileUpdate = (extraFields: Record<string, any> = {}) => {
      const payload: UserProfile & Record<string, any> = {
        ...this.user!,
        avatarUrl: this.avatarPreview || this.user!.avatarUrl || '',
        ...extraFields
      };

      this.userService.updateProfile(this.user!.id, payload).subscribe({
        next: (updated) => {
          this.user = updated;
          this.originalUser = { ...updated };
          this.avatarPreview = updated.avatarUrl || updated.photoUrl || null;
          this.editingProfile = false;
          this.editingContact = false;
          this.selectedAvatarFile = null;
          this.uploadingAvatar = false;
          this.photoValidationState = 'neutral';
          this.cdr.detectChanges();
          alert('Perfil actualizado correctamente');
        },
        error: (err) => {
          console.error('Error actualizando perfil', err);
          this.uploadingAvatar = false;
          alert('No se pudo actualizar el perfil');
        }
      });
    };

    if (this.selectedAvatarFile) {
      this.uploadingAvatar = true;
      this.userService.uploadPhoto(this.user.id, this.selectedAvatarFile).subscribe({
        next: (res: any) => {
          const newUrl = res?.photoUrl || res?.avatarUrl || res?.photo?.url;
          const newPath = res?.photoPath;
          if (newUrl) {
            this.avatarPreview = newUrl;
          }
          applyProfileUpdate({ photoUrl: newUrl, avatarUrl: newUrl, photoPath: newPath });
        },
        error: (err) => {
          console.error('Error subiendo la foto', err);
          this.uploadingAvatar = false;
          alert('No se pudo subir la nueva foto');
        }
      });
      return;
    }

    applyProfileUpdate();
  }

  onEditPreferences() {
    this.router.navigate(['/dashboard/settings']);
  }

  onUsernameInput(value: string) {
    if (!this.user) return;
    this.usernameTouched = true;
    this.user.username = value || '';
    this.usernameAvailable = null;
    this.usernameRequired = !this.user.username || this.user.username.trim().length === 0;
    if (this.usernameRequired) { this.cdr.detectChanges(); return; }
    this.usernameCheck$.next(this.user.username.trim());
  }

}
