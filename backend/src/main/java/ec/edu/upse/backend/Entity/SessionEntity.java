package ec.edu.upse.backend.Entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Document(collection = "Sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionEntity {

    @Id
    private String id;

    private String sessionId;       // Identificador único de sesión
    private String userId;
    private String token;           // JWT único por dispositivo
    private String status;          // active | inactive
    private String device;
    private String ipAddress;
    private String location;
    private String browser;

    private Instant loginAt = Instant.now();
    private Instant lastActivity = Instant.now();
    private Instant expiresAt;      // Expiración del JWT

    private boolean valid = true;   // Para invalidar sesiones
}
