import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-userprofile',
  standalone: true,
  imports: [CommonModule, RouterModule], // Agregar RouterModule aquí
  templateUrl: './userprofile.html',
  styleUrls: ['./userprofile.scss']
})
export class UserProfileComponent {
  user = {
    name: 'Elena Mendoza',
    bio: 'Apasionada por el diseño UI/UX y el desarrollo frontend. Disfruto creando experiencias de usuario intuitivas y visualmente atractivas. Siempre buscando aprender y crecer en el mundo tecnológico.',
    email: 'elena.mendoza@ejemplo.com',
    phone: '+34 678 123 456',
    notifications: true,
    theme: 'claro'
  };

  constructor(private router: Router) {}

  onCancel() {
    // Lógica para cancelar
    console.log('Cancelar clicked');
    this.router.navigate(['/dashboard']);
  }

  onBackToChat() {
    // Lógica para volver al chat
    console.log('Volver a Chat clicked');
    this.router.navigate(['/dashboard']);
  }

  onEditContact() {
    // Lógica para editar contacto
    console.log('Editar Contacto clicked');
  }

  onEditPreferences() {
    // Lógica para editar preferencias
    console.log('Editar Preferencias clicked');
  }
}