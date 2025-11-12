package ec.edu.upse.backend.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.upse.backend.Entity.MessageEntity;
import ec.edu.upse.backend.Service.MessageService;

@RestController
@RequestMapping("/app/v1/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;
    @PostMapping
    public ResponseEntity<MessageEntity> create(@RequestBody MessageEntity message) {
        return ResponseEntity.ok(messageService.save(message));
    }
    @GetMapping
    public ResponseEntity<List<MessageEntity>> getAll() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }
    @GetMapping("/channel/{id}")
    public ResponseEntity<MessageEntity> getById(@PathVariable String id) {
        return messageService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<MessageEntity>> getBySender(@PathVariable String senderId) {
        return ResponseEntity.ok(messageService.getMessagesBySender(senderId));
    }

    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<MessageEntity>> getByReceiver(@PathVariable String receiverId) {
        return ResponseEntity.ok(messageService.getMessagesByReceiver(receiverId));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<MessageEntity>> getByChannel(@PathVariable String channelId) {
        return ResponseEntity.ok(messageService.getMessagesByChannel(channelId));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<MessageEntity> update(@PathVariable String id, @RequestBody MessageEntity newData) {
        MessageEntity updated = messageService.updateMessage(id, newData);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = messageService.deleteMessage(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
