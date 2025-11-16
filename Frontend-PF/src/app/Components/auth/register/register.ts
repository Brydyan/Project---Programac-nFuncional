import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { AuthService } from '../../../Service/AuthService';
import { UserAvailabilityService } from '../../../Service/UserAvailabilityService';
import { ToastService } from '../../../Shared/toast.service';
import { ChangeDetectorRef } from '@angular/core';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

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
    year: ["year", "month", "day"]
  };

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private toast: ToastService,
    private availabilityService: UserAvailabilityService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email,
        ],
        [
          this.asyncEmailValidator()
        ]
      ],

      nombre: ['', Validators.required],

      alias: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(20),
          Validators.pattern(/^[A-Za-z0-9_.]+$/)
        ],
        [
          this.asyncAliasValidator()
        ]
      ],

      password: [
        '',
        [
          Validators.required,
          Validators.minLength(8),
          Validators.pattern(/^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&\-_.:,;#()[\]{}+=|/~`^\\]).+$/)
        ]
      ],

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

  /** ASYNC VALIDATOR: EMAIL */
  asyncEmailValidator() {
    return (control: AbstractControl) => {
      if (!control.value) return of(null);

      return this.availabilityService.checkEmailAvailability(control.value).pipe(
        map((available: boolean) => available ? null : { emailTaken: true }),
        catchError(() => of({ emailError: true }))
      );
    };
  }

  /** ASYNC VALIDATOR: ALIAS */
  asyncAliasValidator() {
    return (control: AbstractControl) => {
      if (!control.value) return of(null);

      return this.availabilityService.checkUsernameAvailability(control.value).pipe(
        map((available: boolean) => available ? null : { aliasTaken: true }),
        catchError(() => of({ aliasError: true }))
      );
    };
  }

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  /** NAVIGATION SYSTEM ⬆️⬇️⬅️➡️ */
  onKeyNavigate(event: KeyboardEvent, currentId: string) {
    const key = event.key;
    const vertical = this.fieldOrder;
    const hGroup = this.horizontalGroups[currentId];
    const index = vertical.indexOf(currentId);

    if (key === "Backspace") {
      const el = document.getElementById(currentId) as HTMLInputElement;
      if (el?.value.length === 0 && index > 0) {
        event.preventDefault();
        this.focus(vertical[index - 1]);
      }
      return;
    }

    if (key === "ArrowUp" && index > 0) {
      event.preventDefault();
      this.focus(vertical[index - 1]);
      return;
    }

    if (key === "ArrowDown" && index < vertical.length - 1) {
      event.preventDefault();
      this.focus(vertical[index + 1]);
      return;
    }

    if (hGroup) {
      const hx = hGroup.indexOf(currentId);

      if (key === "ArrowLeft" && hx > 0) {
        event.preventDefault();
        this.focus(hGroup[hx - 1]);
        return;
      }

      if (key === "ArrowRight" && hx < hGroup.length - 1) {
        event.preventDefault();
        this.focus(hGroup[hx + 1]);
        return;
      }
    }

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

  /** PASSWORD VALIDATOR */
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirm = control.get('confirmPassword')?.value;
    return password === confirm ? null : { mismatch: true };
  }

  /** MOVIMIENTO AUTOMÁTICO FECHA */
  onDigitsInput(event: Event, nextId: string, maxLen: number = 2) {
    const input = event.target as HTMLInputElement;
    const cleaned = input.value.replace(/\D/g, '');
    input.value = cleaned;

    const controlName = input.getAttribute('formControlName');
    if (controlName) {
      this.registerForm.get(controlName)?.setValue(cleaned);
    }

    if (cleaned.length >= maxLen) {
      this.focus(nextId);
    }
  }

  /** FECHA DE NACIMIENTO */
  validateBirthdate(control: AbstractControl): ValidationErrors | null {
    const d = +control.get('day')?.value;
    const m = +control.get('month')?.value;
    const y = +control.get('year')?.value;

    if (!d || !m || !y) return { invalidDate: true };

    if (d < 1 || d > 31) return { invalidDay: true };
    if (m < 1 || m > 12) return { invalidMonth: true };
    if (y < 1900 || y > new Date().getFullYear()) return { invalidYear: true };

    const date = new Date(y, m - 1, d);
    if (
      date.getDate() !== d ||
      date.getMonth() !== m - 1 ||
      date.getFullYear() !== y
    ) return { nonexistentDate: true };

    const today = new Date();
    const minAgeDate = new Date(today.getFullYear() - 13, today.getMonth(), today.getDate());
    if (date > minAgeDate) return { tooYoung: true };

    const maxAgeDate = new Date(today.getFullYear() - 120, today.getMonth(), today.getDate());
    if (date < maxAgeDate) return { tooOld: true };

    return null;
  }

  /** SUBMIT */
  onRegisterSubmit() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.auth.register(this.registerForm.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.toast.show("Registro exitoso", "success");
        this.changeMode.emit("login");
      },
      error: (err) => {
        this.isLoading = false;
        this.toast.show("Error al registrar", "error");
      }
    });
  }

  onGoToLogin() {
    this.changeMode.emit('login');
  }

  get email() { return this.registerForm.get('email'); }
  get alias() { return this.registerForm.get('alias'); }
  get password() { return this.registerForm.get('password'); }
  get confirmPassword() { return this.registerForm.get('confirmPassword'); }
  get day() { return this.registerForm.get('day'); }
  get month() { return this.registerForm.get('month'); }
  get year() { return this.registerForm.get('year'); }
}
