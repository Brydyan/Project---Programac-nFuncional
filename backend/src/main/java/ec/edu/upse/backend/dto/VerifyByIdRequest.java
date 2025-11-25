package ec.edu.upse.backend.dto;

import lombok.Data;

@Data
public class VerifyByIdRequest {
    private String userId;
    private String password;
}
