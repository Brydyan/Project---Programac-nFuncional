import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

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

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onForgotPasswordSubmit(): void {
    if (this.forgotForm.valid) {
      console.log('Forgot Password Email:', this.forgotForm.value);
      // Lógica para enviar correo...
    } else {
      this.forgotForm.markAllAsTouched();
    }
  }

  onGoToLogin(): void {
    this.changeMode.emit('login');
  }

  get email() { return this.forgotForm.get('email'); }
}