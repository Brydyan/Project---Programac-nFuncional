package ec.edu.upse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {

    private String id;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl; // por ahora null, luego podemos añadir campo en UserEntity
}