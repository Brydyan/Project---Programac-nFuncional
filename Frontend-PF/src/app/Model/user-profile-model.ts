export interface UserProfile {
  id: string;
  username: string;
  displayName: string;
  email: string;
  bio?: string;
  statusMessage?: string;
  avatarUrl?: string;
}