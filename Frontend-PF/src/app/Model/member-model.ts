export interface MemberModel {
    id: string;         // opcional, puedes usar igual que userId
    userId: string;     // corresponde al ID real del usuario
    username: string;   // nombre de usuario
    online?: boolean;   // opcional, para indicar si está activo
}
