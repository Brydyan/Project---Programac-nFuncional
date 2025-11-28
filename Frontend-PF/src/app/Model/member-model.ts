export interface MemberModel {
    id: string;         // opcional, puedes usar igual que userId
    userId: string;     // corresponde al ID real del usuario
    username: string;   // nombre de usuario
    online?: boolean;   // opcional, para indicar si está activo
    status?: 'ONLINE' | 'INACTIVE' | 'OFFLINE'; // estado granular
    avatarUrl?: string; // URL opcional de la foto de perfil
}
