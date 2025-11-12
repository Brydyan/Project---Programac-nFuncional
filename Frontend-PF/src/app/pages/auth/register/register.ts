import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './register.html',
  styleUrls: ['./register.scss']
})
export class RegisterComponent implements OnInit {

  registerForm!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      nombre: ['', Validators.required],
      alias: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      fechaNacimiento: this.fb.group({
        dd: ['', [Validators.required, Validators.pattern('^[0-9]{1,2}$')]],
        mm: ['', [Validators.required, Validators.pattern('^[0-9]{1,2}$')]],
        aa: ['', [Validators.required, Validators.pattern('^[0-9]{4}$')]]
      })
    });
  }

  onRegisterSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    console.log('✅ Formulario válido:', this.registerForm.value);
    // Aquí puedes llamar a tu servicio de registro o API
  }

  // === Getters para acceder fácilmente a los campos ===
  get regNombre() { return this.registerForm.get('nombre'); }
  get regAlias() { return this.registerForm.get('alias'); }
  get regEmail() { return this.registerForm.get('email'); }
  get regPassword() { return this.registerForm.get('password'); }
  get regFechaNacimiento() { return this.registerForm.get('fechaNacimiento'); }
  get regDd() { return this.registerForm.get('fechaNacimiento.dd'); }
  get regMm() { return this.registerForm.get('fechaNacimiento.mm'); }
  get regAa() { return this.registerForm.get('fechaNacimiento.aa'); }
}
