package ec.edu.upse.backend.dto;

import lombok.Data;

@Data
public class UserProfileDto {
    private String id;
    private String username;
    private String displayName;
    private String email;
    private String bio;
    private String statusMessage;
    private String avatarUrl;
}
