import { Component, EventEmitter, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../Service/AuthService';
import { Router } from '@angular/router';
import { ToastService } from '../../../Shared/toast.service';
import { UserAvailabilityService } from '../../../Service/UserAvailabilityService';
import { take, timeout } from 'rxjs/operators';
import { TimeoutError } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.scss']
})
export class LoginComponent implements OnInit {

  @Output() changeMode = new EventEmitter<'register' | 'forgot'>();

  loginForm!: FormGroup;
  showPassword = false;
  isLoading = false;
  message: string | null = null;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private toast: ToastService,
    private availability: UserAvailabilityService
    , private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  onLoginSubmit(): void {
    if (!this.loginForm.valid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const payload = {
      identifier: this.loginForm.value.email,
      password: this.loginForm.value.password,
    };

    this.isLoading = true;
    this.message = null;
    // Check email existence but guard against hanging by timing out after 3s
    this.availability.checkEmailAvailability(payload.identifier).pipe(
      take(1),
      timeout(3000)
    ).subscribe({
      next: (available) => {
        // Si está disponible=true => NO existe en la BD
        if (available === true) {
          this.isLoading = false;
          this.message = 'El correo no está registrado.';
          // No mostrar toast aquí por petición de UX; el mensaje ya aparece en la UI.
          this.cdr.detectChanges();
          return;
        }
        // email exists -> proceed to login
        this.performLogin(payload);
      },
      error: (err: any) => {
        this.isLoading = false;
        if (err instanceof TimeoutError || err?.name === 'TimeoutError') {
          this.message = 'La verificación tardó demasiado. Intenta de nuevo.';
        } else {
          this.message = 'No se pudo verificar el correo. Intenta de nuevo.';
        }
        this.toast.show(this.message, 'error');
        this.cdr.detectChanges();
      }
    });
  }

  private performLogin(payload: { identifier: string; password: string }): void {
    this.auth.login(payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;

        // Guardar token si existe
        if (res?.token) {
          localStorage.setItem('token', res.token);
        }

        this.toast.show('Inicio de sesión correcto', 'success');
        this.router.navigate(['/dashboard']);
      },

      error: (err) => {
        this.isLoading = false;

        // Mensaje enviado por backend
        if (err?.error?.message) {
          this.message = err.error.message;
        } else {
          this.message = 'Usuario o contraseña incorrectos';
        }

        this.toast.show(this.message ?? '', 'error');
        this.cdr.detectChanges();
      }
    });
  }

  onForgotPassword(): void {
    this.changeMode.emit('forgot');
  }

  onGoToRegister(): void {
    this.changeMode.emit('register');
  }

  get email() { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }
}
