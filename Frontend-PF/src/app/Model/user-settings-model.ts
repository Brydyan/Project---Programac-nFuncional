export interface UserSettingsDto {
  id: string;
  notificationsActivate: boolean | null;
  notificationsSound: boolean | null;
  notificationsDesktop: boolean | null;
  darkMode: boolean | null;
  fontSize: number | null;
  interfaceLanguage: string | null;
  timezone: string | null;
}