import { Routes } from '@angular/router';
import { AuthComponent } from './pages/auth/auth';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/dashboard/dashboard').then(m => m.Dashboard),
    children: [
      {
        path: 'conversations',
        loadComponent: () =>
          import('./pages/dashboard/conversations/conversations')
            .then(m => m.Conversations)
      },
      {
        path: 'chat/:contactId',
        loadComponent: () =>
          import('./pages/dashboard/chat-thread/chat-thread')
            .then(m => m.ChatThread)
      },
      // ✅ NUEVAS RUTAS AGREGADAS
      {
        path: 'settings',
        loadComponent: () =>
          import('./Components/user-settings/user-settings')
            .then(m => m.UserSettings)
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./Components/userprofile/userprofile')
            .then(m => m.UserProfileComponent)
      },
      /*{
        path: 'channels',
        loadComponent: () =>
          import('./pages/dashboard/channels/channels')
            .then(m => m.Channels)
      },*/
      // Por defecto, abre la lista de conversaciones
      { path: '', redirectTo: 'conversations', pathMatch: 'full' }
    ]
  },
  { path: '', redirectTo: 'auth', pathMatch: 'full' }
];