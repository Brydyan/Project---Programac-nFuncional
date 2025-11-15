package ec.edu.upse.backend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier; // username or email
    private String password;
}
