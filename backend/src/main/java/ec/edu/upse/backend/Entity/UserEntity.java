package ec.edu.upse.backend.Entity;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection="Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    private String id; // MongoDB usa String una cadena de 12 bytes (ObjectID)
    @Field("username")
    private String username;
    private String displayName;
    private String email;
    @Field("password")
    private String password;
    private Instant createdAt = Instant.now();
    private String status; // ONLINE, OFFLINE, AWAY
    // Subdocumento embebido
    private Map<String, Object> preferences; // Ejemplo: { "theme": "dark", "notifications": true }
    
    // Campos adicionales para mapeo con frontend
    private String alias; // Sinónimo de username
    private String nombre; // Sinónimo de displayName
    private String confirmPassword; // Solo para validación, no se guarda
    private String day;
    private String month;
    private String year;
    
    // Método auxiliar para asignar username desde alias si es necesario
    public void processAliasAndNombre() {
        if (this.alias != null && !this.alias.isEmpty() && this.username == null) {
            this.username = this.alias;
        }
        if (this.nombre != null && !this.nombre.isEmpty() && this.displayName == null) {
            this.displayName = this.nombre;
        }
    }
}
