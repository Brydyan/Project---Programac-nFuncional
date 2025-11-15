import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- Importar
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms'; // <-- Importar
import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-login',
  standalone: true, 
  imports: [
    CommonModule,
    ReactiveFormsModule,
  ], 
  templateUrl: './login.html',
  styleUrls: ['./login.scss'] 
})
export class LoginComponent implements OnInit {
  @Output() changeMode = new EventEmitter<'register' | 'forgot'>();
  loginForm!: FormGroup;
  showPassword = false;

  constructor(private fb: FormBuilder, private http: HttpClient) { }

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
    
    // 🎯 URL FINAL CORREGIDA
    const loginUrl = '/api/auth/login'; 
    
    this.http.post(loginUrl, this.loginForm.value).subscribe({
      next: (response) => {
        console.log('Login exitoso:', response);
        // Aquí debes guardar el token (si existe) y redirigir al usuario
      },
      error: (err) => {
        console.error('Error de login:', err);
        // Muestra un mensaje de error en la UI (e.g., credenciales incorrectas)
      }
    });
    
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