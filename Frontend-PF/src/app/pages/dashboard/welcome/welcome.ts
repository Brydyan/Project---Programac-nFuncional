import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-dashboard-welcome',
  imports: [CommonModule],
  template: `
    <div class="welcome">
      <h2>Bienvenido a YARG Flow 👋</h2>
      <p>Selecciona una conversación o crea una nueva para empezar.</p>
    </div>
  `,
  styleUrls: ['./welcome.css']
})
export class DashboardWelcome {}
