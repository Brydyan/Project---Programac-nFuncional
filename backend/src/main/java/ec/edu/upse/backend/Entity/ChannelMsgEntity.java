package ec.edu.upse.backend.Entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document(collection = "ChannelMessages")
@Getter
@Setter

public class ChannelMsgEntity {
    @Id
    private String id;
    private String messageId; // ID del mensaje
    private String channel; // ID del canal
    private String senderId; // ID del usuario que envía el mensaje
    private String messageContent; // Contenido del mensaje
    private Instant timestamp = Instant.now(); // Marca de tiempo del mensaje
    private String status; // Estado del mensaje (pendiente,enviado, entregado, leído)

    public void setId(String id){
        this.id = id;
    }
}
