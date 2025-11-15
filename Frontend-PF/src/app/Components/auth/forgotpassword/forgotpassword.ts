import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../Service/AuthService';
import { ToastService } from '../../../Shared/toast.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './forgotpassword.html',
  styleUrls: ['./forgotpassword.scss']
})
export class ForgotPasswordComponent implements OnInit {

  @Output() changeMode = new EventEmitter<'login'>();
  forgotForm!: FormGroup;
  isLoading = false;
  message: string | null = null;

  constructor(private fb: FormBuilder, private auth: AuthService, private toast: ToastService) { }

  ngOnInit(): void {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onForgotPasswordSubmit(): void {
    if (this.forgotForm.valid) {
      this.isLoading = true;
      this.message = null;
      const email = this.forgotForm.value.email;
      this.auth.forgotPassword(email).subscribe({
        next: (user) => {
          this.isLoading = false;
          if (user) {
            this.message = 'Se ha enviado el correo de recuperación (simulado).';
            this.toast.show('Se ha enviado el correo de recuperación (simulado).', 'success');
          } else {
            this.message = 'No se encontró una cuenta con ese correo.';
            this.toast.show(this.message, 'error');
          }
        },
        error: (err) => {
          this.isLoading = false;
          console.error('Error en forgotPassword:', err);
          this.message = 'Error al procesar la solicitud.';
          this.toast.show(this.message, 'error');
        }
      });
    } else {
      this.forgotForm.markAllAsTouched();
    }
  }

  onGoToLogin(): void {
    this.changeMode.emit('login');
  }

  get email() { return this.forgotForm.get('email'); }
}