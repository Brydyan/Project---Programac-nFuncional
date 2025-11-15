import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- Importar
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms'; // <-- Importar
import { AuthService } from '../../../Service/AuthService';
import { ToastService } from '../../../Shared/toast.service';

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
  isLoading = false;
  message: string | null = null;

  constructor(private fb: FormBuilder, private auth: AuthService, private toast: ToastService) { }

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.pattern(/^[^@\s]+@[^@\s]+\.[^@\s]+$/)
      ]],
      nombre: ['', Validators.required],
      alias: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(20),
        Validators.pattern(/^[A-Za-z0-9_.]+$/)
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&\-_.:,;#()[\]{}+=|/~`^\\]).+$/)
      ]],
      confirmPassword: ['', Validators.required],
      day: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
      month: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]],
      year: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(4)]]
      }, {
        validators: [
          this.passwordMatchValidator,
          this.validateBirthdate.bind(this)
        ]
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

  focusNextTo(id: string): void {
    const el = document.getElementById(id);
    if (el) el.focus();
  }

  onDigitsInput(event: Event, nextId: string, maxLen: number = 2): void {
    const input = event.target as HTMLInputElement;

    // eliminar caracteres no numéricos
    const cleaned = input.value.replace(/\D/g, '');
    input.value = cleaned;

    // actualizar en el formulario
    const controlName = input.getAttribute('formControlName');
    if (controlName) {
      this.registerForm.get(controlName)?.setValue(cleaned);
    }

    // mover automáticamente
    if (cleaned.length >= maxLen) {
      this.focusNextTo(nextId);
    }
  }


  onRegisterSubmit(): void {
    if (this.registerForm.valid) {
      this.isLoading = true;
      this.message = null;
      const payload = this.registerForm.value;
      this.auth.register(payload).subscribe({
        next: (user) => {
          this.isLoading = false;
          console.log('Usuario registrado:', user);
          this.message = 'Registro exitoso. Inicia sesión.';
          this.toast.show('Registro exitoso. Inicia sesión.', 'success');
          // Cambiar a vista de login
          this.changeMode.emit('login');
        },
        error: (err) => {
          this.isLoading = false;
          console.error('Error al registrar usuario:', err);
          // Extraer mensaje de error del servidor
          let errorMsg = 'Error al registrar usuario';
          if (err?.error?.message) {
            errorMsg = err.error.message;
          } else if (err?.error) {
            errorMsg = typeof err.error === 'string' ? err.error : err.error.message || errorMsg;
          } else if (err?.message) {
            errorMsg = err.message;
          }
          this.message = errorMsg;
          this.toast.show(errorMsg, 'error');
        }
      });
    } else {
      this.registerForm.markAllAsTouched();
    }
  }
  onGoToLogin(): void {
    this.changeMode.emit('login');
  }
    validateBirthdate(control: AbstractControl): ValidationErrors | null {
    const day = control.get('day')?.value;
    const month = control.get('month')?.value;
    const year = control.get('year')?.value;

    if (!day || !month || !year) return { invalidDate: true };

    const d = parseInt(day, 10);
    const m = parseInt(month, 10);
    const y = parseInt(year, 10);

    // Validar rangos básicos
    if (isNaN(d) || isNaN(m) || isNaN(y)) return { invalidDate: true };
    if (d < 1 || d > 31) return { invalidDay: true };
    if (m < 1 || m > 12) return { invalidMonth: true };
    if (y < 1900 || y > new Date().getFullYear()) return { invalidYear: true };

    // Validar que la fecha exista
    const date = new Date(y, m - 1, d);
    if (
      date.getFullYear() !== y ||
      date.getMonth() !== (m - 1) ||
      date.getDate() !== d
    ) {
      return { nonexistentDate: true };
    }

    // Validar edad mínima (13 años)
    const today = new Date();
    const minAgeDate = new Date(today.getFullYear() - 13, today.getMonth(), today.getDate());
    if (date > minAgeDate) {
      return { tooYoung: true };
    }

    // Validar edad máxima (120 años)
    const maxAgeDate = new Date(today.getFullYear() - 120, today.getMonth(), today.getDate());
    if (date < maxAgeDate) {
      return { tooOld: true };
    }

    return null;
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