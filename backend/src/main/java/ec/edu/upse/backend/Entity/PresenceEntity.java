package ec.edu.upse.backend.Entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "Presence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresenceEntity {
    @Id
    private String id;

    private String userId;   // Referencia al usuario
    private String status;   // ONLINE, OFFLINE, AWAY
    private Instant lastSeen = Instant.now();
}
