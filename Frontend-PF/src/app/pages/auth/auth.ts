import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from '../../Components/auth/login/login';
import { RegisterComponent } from '../../Components/auth/register/register';
import { ForgotPasswordComponent } from '../../Components/auth/forgotpassword/forgotpassword';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, LoginComponent, RegisterComponent, ForgotPasswordComponent],
  templateUrl: './auth.html',
  styleUrls: ['./auth.css']
})
export class AuthComponent {
  
  
  mode: 'login' | 'register' | 'forgot' = 'login'; 
  isAnimating = false;

  setMode(newMode: 'login' | 'register' | 'forgot'): void {
    if (this.isAnimating || this.mode === newMode) return;

    this.isAnimating = true;
    this.mode = newMode; 

    // Sincronizado con la animación CSS
    setTimeout(() => (this.isAnimating = false), 700);
  }
 
}