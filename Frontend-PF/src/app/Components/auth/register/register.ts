import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- Importar
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms'; // <-- Importar

@Component({
  selector: 'app-register',
  standalone: true, 
  imports: [
    CommonModule,
    ReactiveFormsModule
  ], 
  templateUrl: './register.html',
  styleUrls: ['./register.scss'] 
})
export class RegisterComponent implements OnInit {
  
  @Output() changeMode = new EventEmitter<'login'>();
  registerForm!: FormGroup;
  showPassword = false;

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      nombre: ['', Validators.required],
      alias: ['', Validators.required],
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required],
      day: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
      month: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
      year: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(4)]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password?.value !== confirmPassword?.value) {
      return { mismatch: true };
    }
    return null;
  }

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  onRegisterSubmit(): void {
    if (this.registerForm.valid) {
      console.log('Register data:', this.registerForm.value);
    } else {
      this.registerForm.markAllAsTouched();
    }
  }
  onGoToLogin(): void {
    this.changeMode.emit('login');
  }
  

  get email() { return this.registerForm.get('email'); }
  get nombre() { return this.registerForm.get('nombre'); }
  get alias() { return this.registerForm.get('alias'); }
  get password() { return this.registerForm.get('password'); }
  get confirmPassword() { return this.registerForm.get('confirmPassword'); }
  get day() { return this.registerForm.get('day'); }
  get month() { return this.registerForm.get('month'); }
  get year() { return this.registerForm.get('year'); }
}