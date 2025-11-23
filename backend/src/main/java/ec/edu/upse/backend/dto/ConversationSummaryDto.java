package ec.edu.upse.backend.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryDto {

    private String id;          // id de la “conversación” (aquí será el otro userId)
    private String name;        // nombre a mostrar
    private String lastMessage; // último mensaje de esa conversación
    private Instant lastTime;   // fecha/hora del último mensaje
    private String avatarUrl;   // luego podemos enriquecer
    private int unreadCount;    // más adelante lo calculamos con Notifications
}