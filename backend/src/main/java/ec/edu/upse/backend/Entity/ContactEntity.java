package ec.edu.upse.backend.Entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
@Document(collection = "Contacts")
@Getter
@Setter
public class ContactEntity {
    @Id
    private String id;
    private String userId;
    private String contactId;
    private String state; // pending, accepted, blocked
    private Instant createdAt = Instant.now();
}
