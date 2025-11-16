import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- Importar
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms'; // <-- Importar
import { AuthService } from '../../../Service/AuthService';
import { UserAvailabilityService } from '../../../Service/UserAvailabilityService';
import { ToastService } from '../../../Shared/toast.service';
import { ChangeDetectorRef } from '@angular/core';


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

  // Availability check states
  usernameAvailability: { available?: boolean; checking: boolean } = { checking: false };
  emailAvailability: { available?: boolean; checking: boolean } = { checking: false };

    /** Orden vertical */
  fieldOrder = [
    "email",
    "nombre",
    "alias",
    "password",
    "confirmPassword",
    "day",
    "month",
    "year",
    "submitBtn"
  ];

  /** Grupos horizontales */
  horizontalGroups: Record<string, string[]> = {
    day: ["day", "month", "year"],
    month: ["day", "month", "year"],
    year: ["day", "month", "year"]
  };

  constructor(private fb: FormBuilder, private auth: AuthService, private toast: ToastService, private availabilityService: UserAvailabilityService, private cdr: ChangeDetectorRef) { }

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
/** Navegación mejorada */
  onKeyNavigate(event: KeyboardEvent, currentId: string) {
    const key = event.key;
    const vertical = this.fieldOrder;
    const hGroup = this.horizontalGroups[currentId];
    const index = vertical.indexOf(currentId);

    // Backspace retrocede si está vacío
    if (key === "Backspace") {
      const el = document.getElementById(currentId) as HTMLInputElement;
      if (el?.value.length === 0 && index > 0) {
        event.preventDefault();
        this.focus(vertical[index - 1]);
      }
      return;
    }

    // Flecha arriba (vertical)
    if (key === "ArrowUp" && index > 0) {
      event.preventDefault();
      this.focus(vertical[index - 1]);
      return;
    }

    // Flecha abajo (vertical)
    if (key === "ArrowDown" && index < vertical.length - 1) {
      event.preventDefault();
      this.focus(vertical[index + 1]);
      return;
    }

    // Flechas horizontales (solo en day/month/year)
    if (hGroup) {
      const hIndex = hGroup.indexOf(currentId);

      if (key === "ArrowLeft" && hIndex > 0) {
        event.preventDefault();
        this.focus(hGroup[hIndex - 1]);
        return;
      }

      if (key === "ArrowRight" && hIndex < hGroup.length - 1) {
        event.preventDefault();
        this.focus(hGroup[hIndex + 1]);
        return;
      }
    }

    // Enter → avanzar al siguiente campo
    if (key === "Enter" && index < vertical.length - 1) {
      event.preventDefault();
      this.focus(vertical[index + 1]);
    }
  }

  focus(id: string) {
    setTimeout(() => {
      const el = document.getElementById(id);
      if (el) el.focus();
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

  // Availability check methods
  onAliasBlur() {
    const c = this.alias;
    if (c?.valid) {
      this.usernameAvailability.checking = true;

      this.availabilityService.checkUsernameAvailability(c.value)
        .subscribe((available: boolean) => {

          this.usernameAvailability.available = available;
          this.usernameAvailability.checking = false;

          this.cdr.detectChanges(); // 👈 refresco inmediato de UI
        });
    }
  }



  onEmailBlur() {
    const c = this.email;
    if (c?.valid) {
      this.emailAvailability.checking = true;

      this.availabilityService.checkEmailAvailability(c.value)
        .subscribe((available: boolean) => {

          this.emailAvailability.available = available;
          this.emailAvailability.checking = false;

          this.cdr.detectChanges(); // 👈 refresco inmediato
        });
    }
  }




  onRegisterSubmit(): void {
    // Check if username and email are available before submitting
    if (this.usernameAvailability.available === false || this.emailAvailability.available === false) {
      this.message = 'Por favor, usa un alias y correo únicos.';
      this.toast.show('Por favor, usa un alias y correo únicos.', 'error');
      return;
    }

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