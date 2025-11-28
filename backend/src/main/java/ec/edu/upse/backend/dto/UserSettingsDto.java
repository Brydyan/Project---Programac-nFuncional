package ec.edu.upse.backend.dto;

import lombok.Data;

@Data
public class UserSettingsDto {
    private String id;
    private Boolean notificationsActivate;
    private Boolean notificationsSound;
    private Boolean notificationsDesktop;
    private Boolean darkMode;
    private Integer fontSize;
    private String interfaceLanguage;
    private String timezone;
}
