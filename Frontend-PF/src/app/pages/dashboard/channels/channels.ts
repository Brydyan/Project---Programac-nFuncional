import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ChannelsModel } from '../../../Model/channels-models';
import { Subscription } from 'rxjs';
import { ChannelsService } from '../../../Service/channels.service';
import { SessionService } from '../../../Service/session.service';
import { ChannelEventsService } from '../../../Service/channel-event.service';
import { RealtimeService } from '../../../Service/realtime.service';

@Component({
  selector: 'app-channels',
  standalone: true,
  imports: [CommonModule,FormsModule,RouterLink],
  templateUrl: './channels.html',
  styleUrl: './channels.css',
})
export class Channels implements OnInit, OnDestroy{
  channels: ChannelsModel[] = [];
  loading = true;
  initialLoadDone = false;
  error: string | null = null;
  currentUserId: string | null = null;

  // Modal "Unirse a un canal"
  showJoinPanel = false;
  joinSearch = '';
  searchResults: ChannelsModel[] = [];

  private eventsSub?: Subscription;
  private wsSub?: Subscription;

  constructor(
    private router: Router,
    private channelService: ChannelsService,
    private sessionService: SessionService,
    private channelEvents: ChannelEventsService,
    private realtime: RealtimeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const token = localStorage.getItem('token');
    if (!token) {
      this.loading = false;
      this.error = 'No se encontró token de sesión';
      return;
    }

    // Cargar sesión del usuario
    this.sessionService.getByToken(token!).subscribe({
      next: (session) => {
        this.currentUserId = session.userId;
        this.loadChannels(true);

        // Solo suscribirse a eventos si hay userId
        if (this.currentUserId) {
          this.eventsSub = this.channelEvents.refresh$
            .subscribe(() => this.loadChannels(false));

          this.wsSub = this.realtime
            .subscribeToChannelEvents(this.currentUserId)
            .subscribe({
              next: (event) => this.applyChannelEvent(event),
              error: (err) => console.error('WS Error:', err)
            });
        }
      },
      error: () => {
        this.loading = false;
        this.error = 'No se pudo obtener la sesión';
        this.cdr.detectChanges();
      }
    });

  }

  ngOnDestroy(): void {
    this.eventsSub?.unsubscribe();
    this.wsSub?.unsubscribe();
  }

  // ==========================
  // Cargar canales
  // ==========================
  loadChannels(showLoading: boolean) {
    if (!this.currentUserId) return;

    if (showLoading && !this.initialLoadDone) {
        this.loading = true;
        this.cdr.detectChanges();
    }

    // ✅ CORRECCIÓN: Usar el nuevo método que filtra por miembro
    this.channelService.getChannelsByMember(this.currentUserId).subscribe({ 
        next: (data) => {
            this.channels = data.filter(c => !!c.id);
            this.initialLoadDone = true;
            this.loading = false;
            this.cdr.detectChanges();
        },
        error: () => {
            this.error = 'No se pudieron cargar los canales';
            this.loading = false;
            this.cdr.detectChanges();
        }
    });
  }

  // ==========================
  // Aplicar eventos recibidos por WS
  // ==========================
  applyChannelEvent(event: any) {
    switch (event.type) {
      case 'CHANNEL_CREATED':
        // Agrega el canal a la lista, pero NO navegar automáticamente
        this.channels = [event.payload, ...this.channels];
        break;

      case 'CHANNEL_DELETED':
        this.channels = this.channels.filter(c => c.id !== event.payload.id);
        break;

      case 'USER_ADDED_TO_CHANNEL':
        if (event.payload.userId === this.currentUserId) {
          this.channels = [event.payload.channel, ...this.channels];
        }
        break;

      case 'NEW_CHANNEL_MESSAGE':
        const idx = this.channels.findIndex(c => c.id === event.payload.channelId);
        if (idx >= 0) {
          this.channels[idx].unread = (this.channels[idx].unread || 0) + 1;
        }
        break;
    }

    this.cdr.detectChanges();
  }


  // ==========================
  // Modal "Unirse a un canal"
  // ==========================
  openJoinPanel() {
    this.showJoinPanel = true;
    this.joinSearch = '';
    this.searchResults = this.channels.filter(c => c.type === 'PUBLIC');
  }

  closeJoinPanel() {
    this.showJoinPanel = false;
  }

  onJoinSearchChange() {
    const term = (this.joinSearch || '').trim();

    if (!term) {
      this.searchResults = [];
      return;
    }

    this.channelService.searchPublic(term).subscribe({
      next: res => (this.searchResults = res),
      error: () => (this.searchResults = [])
    });
  }

  joinSelectedChannel(ch: ChannelsModel) {
    if (!this.currentUserId) return alert('Sesión no encontrada');

    this.channelService.joinChannel(ch.id, this.currentUserId).subscribe({
      next: () => {
        this.channelEvents.emitRefresh();
        this.closeJoinPanel();
      },
      error: (err) => {
        console.error('Error al unirse', err);
        alert('Error al unirse al canal');
      }
    });
  }

  // ==========================
  // Abrir un canal existente
  // ==========================
  openChannel(ch: ChannelsModel) {
    console.log("Abrir canal:", ch);
    this.router.navigate(['/dashboard/channel', ch.id]);

    // Suscribirse a eventos de este canal
    this.wsSub?.unsubscribe();
    this.wsSub = this.realtime
      .subscribeToChannelEvents(this.currentUserId!)
      .subscribe(event => this.applyChannelEvent(event));
  }

  // ==========================
  // Abrir formulario de creación de canal
  // ==========================
  
  
  goCreate(event: Event) {
    event.stopPropagation();  // bloquea clicks de padres
    this.showJoinPanel = false; // cierra modal si estaba abierto
    this.router.navigate(['/dashboard/channel/create']);
  }

}