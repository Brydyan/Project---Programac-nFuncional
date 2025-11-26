package ec.edu.upse.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {
    private String id;

    // Notificaciones
    private Boolean notificationsActivate;
    private Boolean notificationsSound;
    private Boolean notificationsDesktop;

    // Apariencia
    private Boolean darkMode;
    private Integer fontSize;

    // Idioma / zona horaria
    private String interfaceLanguage;
    private String timezone;
}
