import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- Importar
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms'; // <-- Importar

@Component({
  selector: 'app-login',
  standalone: true, 
  imports: [
    CommonModule,
    ReactiveFormsModule
  ], 
  templateUrl: './login.html',
  styleUrls: ['./login.scss'] 
})
export class LoginComponent implements OnInit {
  @Output() changeMode = new EventEmitter<'register' | 'forgot'>();
  loginForm!: FormGroup;
  showPassword = false;

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  onLoginSubmit(): void {
    if (this.loginForm.valid) {
      console.log('Login data:', this.loginForm.value);
    } else {
      this.loginForm.markAllAsTouched();
    }
  }
  onForgotPassword(): void {
    console.log('Emitiendo evento forgot'); //para depurar
    this.changeMode.emit('forgot');
  }

  // Este método emite el evento 'register'
  onGoToRegister(): void {
    this.changeMode.emit('register');
  }

  get email() { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }
}