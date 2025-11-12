package ec.edu.upse.backend.Entity;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Document(collection = "Channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelEntity {
    @Id
    private String id;

    private String name;        // Nombre del canal
    private String type;        // PUBLIC o PRIVATE
    private String ownerId;     // ID del usuario creador

    private List<String> members;  // IDs de los usuarios miembros

    private Instant createdAt = Instant.now();
}
