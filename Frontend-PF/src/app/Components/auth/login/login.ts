import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../Service/AuthService';
import { Router } from '@angular/router';
import { ToastService } from '../../../Shared/toast.service';

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
    private toast: ToastService
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
