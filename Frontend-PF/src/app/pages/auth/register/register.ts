import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router'; // <-- Importante para el enlace

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink // <-- Añadir aquí
  ],
  templateUrl: './register.html',
  styleUrls: ['./register.scss'] // Puedes usar el mismo SCSS si quieres
})
export class RegisterComponent implements OnInit {

  registerForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.registerForm = this.fb.group({
      nombre: ['', [Validators.required]],
      alias: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      fechaNacimiento: this.fb.group({
        dd: ['', [Validators.required, Validators.maxLength(2)]],
        mm: ['', [Validators.required, Validators.maxLength(2)]],
        aa: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(4)]]
      })
    });
  }
  
  ngOnInit(): void { }

  onRegisterSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }
    console.log('Register Submit:', this.registerForm.value);
    // Aquí iría tu lógica de registro
  }

  // Getters para el formulario de registro
  get regNombre() { return this.registerForm.get('nombre'); }
  get regAlias() { return this.registerForm.get('alias'); }
  get regEmail() { return this.registerForm.get('email'); }
  get regPassword() { return this.registerForm.get('password'); }
  get regFechaNacimiento() { return this.registerForm.get('fechaNacimiento'); }
  get regDd() { return this.registerForm.get('fechaNacimiento.dd'); }
  get regMm() { return this.registerForm.get('fechaNacimiento.mm'); }
  get regAa() { return this.registerForm.get('fechaNacimiento.aa'); }
}