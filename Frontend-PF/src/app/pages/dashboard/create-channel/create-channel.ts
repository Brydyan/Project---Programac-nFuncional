import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ChannelsService } from '../../../Service/channels-service';
import { SessionService } from '../../../Service/session.service';
import { ChannelEventsService } from '../../../Service/channel-events-service';

@Component({
  selector: 'app-create-channel',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './create-channel.html',
  styleUrl: './create-channel.css',
})
export class CreateChannel {
  name: string = '';
  type: 'PUBLIC' | 'PRIVATE' = 'PUBLIC';
  ownerId: string | null = null;

  constructor(
    private router: Router,
    private channelService: ChannelsService,
    private sessionService: SessionService,
    private channelEvents: ChannelEventsService
  ) {
    const token = localStorage.getItem('token');
    this.sessionService.getByToken(token!).subscribe(s => {
      this.ownerId = s.userId;
    });
  }

  // ➤ Cerrar modal
  close() {
    this.router.navigate(['/dashboard/channels']);
  }

  // ➤ Crear Canal
  createChannel() {
    if (!this.name.trim() || !this.ownerId) return;

    const newChannel = {
      name: this.name.trim(),
      type: this.type,
      ownerId: this.ownerId,
      members: [this.ownerId]
    };

    this.channelService.create(newChannel).subscribe({
      next: () => {
        this.channelEvents.emitRefresh();   // ← NOTIFICAR A LISTA DE CANALES
        this.close();
      },
      error: () => alert("Error al crear canal")
    });
  }
}
