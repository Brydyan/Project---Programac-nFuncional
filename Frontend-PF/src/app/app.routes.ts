import { Routes } from '@angular/router';
import { AuthComponent } from './pages/auth/auth';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },

  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/dashboard/dashboard').then(m => m.Dashboard),
    children: [
      // 👋 Pantalla de bienvenida por defecto
      {
        path: '',
        loadComponent: () =>
          import('./pages/dashboard/welcome/welcome')
            .then(m => m.DashboardWelcome)
      },

      // 💬 Lista de conversaciones
      {
        path: 'conversations',
        loadComponent: () =>
          import('./pages/dashboard/conversations/conversations')
            .then(m => m.Conversations)
      },

      // 💭 Chat 1 a 1 (ojo: usamos :id porque así lo usa ChatThread)
      {
        path: 'chat/:id',
        loadComponent: () =>
          import('./pages/dashboard/chat-thread/chat-thread')
            .then(m => m.ChatThread)
      },

      // ⚙️ Configuración de usuario
      {
        path: 'settings',
        loadComponent: () =>
          import('./Components/user-settings/user-settings')
            .then(m => m.UserSettings)
      },

      // 👤 Perfil de usuario
      {
        path: 'profile',
        loadComponent: () =>
          import('./Components/userprofile/userprofile')
            .then(m => m.UserProfileComponent)
      },

      {
        path: 'channels',
        loadComponent: () =>
          import('./pages/dashboard/channels/channels')
            .then(m => m.Channels)
      },

      {
        path: 'channel/create',
        loadComponent: () =>
          import('./pages/dashboard/create-channel/create-channel')
            .then(m => m.CreateChannel)
      },
      
      {
        path: 'channel/:id', // esta ruta carga ChannelThread
        loadComponent: () =>
          import('./pages/dashboard/channels-thread/channels-thread')
            .then(m => m.ChannelsThread)
      },
    
    ]
  },

  // raíz de la app → login
  { path: '', redirectTo: 'auth', pathMatch: 'full' }
];
