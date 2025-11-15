import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- Importar
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms'; // <-- Importar
import { AuthService } from '../../../Service/AuthService';
import { ToastService } from '../../../Shared/toast.service';
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
  isLoading = false;
  message: string | null = null;

  constructor(private fb: FormBuilder, private auth: AuthService, private toast: ToastService) { }

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
    
    // Llamar al servicio de autenticación que crea una sesión en el backend
    const identifier = this.loginForm.value.email || this.loginForm.value.alias;
    this.isLoading = true;
    this.message = null;
    this.auth.login(identifier, this.loginForm.value.password).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        console.log('Login (sesión) exitoso:', response);
        const token = response?.token;
        if (token) {
          this.auth.saveToken(token);
        }
        this.message = 'Inicio de sesión correcto.';
        this.toast.show('Inicio de sesión correcto', 'success');
        // TODO: redirigir al usuario a la app
      },
        error: (err: any) => {
        this.isLoading = false;
        console.error('Error al crear sesión:', err);
        this.message = err?.message || 'Error al iniciar sesión.';
        this.toast.show(this.message ?? 'Error al iniciar sesión.', 'error');
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