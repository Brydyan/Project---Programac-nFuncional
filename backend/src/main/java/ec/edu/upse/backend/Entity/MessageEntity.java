package ec.edu.upse.backend.Entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
@Document(collection = "Messages")
@Getter
@Setter
public class MessageEntity {
    @Id
    private String id;
    private String senderId;
    private String receiverId; // null si es canal
    private String channelId;  // null si es chat 1:1
    private String content;
    private Instant timestamp = Instant.now();
    private boolean edited = false;
    private boolean deleted = false;
    // Estado del mensaje: SENT, DELIVERED, READ
    public enum MessageStatus {
        SENT, DELIVERED, READ
    }

    private MessageStatus status = MessageStatus.SENT;
}
