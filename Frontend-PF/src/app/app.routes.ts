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
      path: '',
      loadComponent: () =>
        import('./pages/dashboard/welcome/welcome')
          .then(m => m.DashboardWelcome)   
    },
    {
      path: 'conversations',
      loadComponent: () =>
        import('./pages/dashboard/conversations/conversations')
          .then(m => m.Conversations)
    },
    {
      path: 'chat/:id',
      loadComponent: () =>
        import('./pages/dashboard/chat-thread/chat-thread')
          .then(m => m.ChatThread)
    },
      
    // Si luego activas canales:
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
      }
  ]
},


  { path: '', redirectTo: 'auth', pathMatch: 'full' }
];
