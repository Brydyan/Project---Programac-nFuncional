package ec.edu.upse.backend.Entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
@Document(collection = "Sessions")
@Getter
@Setter

public class SessionEntity {
    @Id
    private String id;
    private String userId; // Referencia al usuario
    private String token; // JWT or session token
    private String status; // active, inactive
    private String device; // dispositivo usado
    private String ipAddress;  // dirección IP
    private String location; // ubicación
    private String browser; // navegador
    private Instant horaFecha = Instant.now(); // marca de tiempo de la sesión
}
