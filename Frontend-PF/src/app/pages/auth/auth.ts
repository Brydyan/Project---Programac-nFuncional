import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from '../../Components/auth/login/login';
import { RegisterComponent } from '../../Components/auth/register/register';
import { ForgotPasswordComponent } from '../../Components/auth/forgotpassword/forgotpassword';
import { AuthService } from '../../Service/AuthService';
import { Router } from '@angular/router';
import { ToastService } from '../../Shared/toast.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, LoginComponent, RegisterComponent, ForgotPasswordComponent],
  templateUrl: './auth.html',
  styleUrls: ['./auth.css']
})
export class AuthComponent implements OnInit {

  constructor(private auth: AuthService, private router: Router, private toast: ToastService) {}

  mode: 'login' | 'register' | 'forgot' = 'login';

  ngOnInit(): void {
    // Auto-login: if there is a token in localStorage, validate it and redirect
    const token = localStorage.getItem('token');
    if (token) {
      this.auth.validateToken(token).subscribe({
        next: (res: any) => {
          if (res) {
            // valid session -> go to dashboard
            this.router.navigate(['/dashboard']);
          }
        },
        error: () => {
          // sesión expirada o inválida
          localStorage.removeItem('token');
          this.toast.show('Tu sesión expiró. Inicia sesión de nuevo.', 'info');
          // te quedas en /auth
        }
      });
    }
  }

  setMode(m: 'login' | 'register' | 'forgot') {
    this.mode = m;
  }
}
