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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.upse.backend.Entity.MessageEntity;
import ec.edu.upse.backend.Service.MessageService;
import ec.edu.upse.backend.dto.DirectMessageDto;
import ec.edu.upse.backend.Service.FirebaseStorageService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app/v1/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private FirebaseStorageService storageService;

    // CREATE
    @PostMapping
    public ResponseEntity<MessageEntity> create(@RequestBody MessageEntity message) {
        return ResponseEntity.ok(messageService.save(message));
    }

    // READ
    @GetMapping
    public ResponseEntity<List<MessageEntity>> getAll() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }

    @GetMapping("/{id}")
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

   @GetMapping("/direct/{userId}/{contactId}")
    public ResponseEntity<List<MessageEntity>> getDirect(
            @PathVariable String userId,
            @PathVariable String contactId
    ) {
        List<MessageEntity> list = messageService.getDirectConversation(userId, contactId);
        return ResponseEntity.ok(list);
    }

    // 👇 NUEVO: enviar mensaje directo
    @PostMapping("/direct")
    public ResponseEntity<MessageEntity> sendDirect(@RequestBody DirectMessageDto dto) {
        MessageEntity saved = messageService.sendDirect(
            dto.getSenderId(),
            dto.getReceiverId(),
            dto.getContent(),
            dto.getAttachmentUrl(),
            dto.getAttachmentPath(),
            dto.getAttachmentName(),
            dto.getAttachmentMime(),
            dto.getAttachmentSize()
        );
        return ResponseEntity.ok(saved);
    }

    // Upload an attachment and return metadata (url, path, name, contentType, size)
    @PostMapping("/attachments")
    public ResponseEntity<?> uploadAttachment(@RequestParam(value = "file") MultipartFile file,
                                              @RequestParam(value = "folder", required = false) String folder) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }

        String contentType = file.getContentType();
        long size = file.getSize();

        // Size limits: images <= 10MB, others <= 50MB
        long IMAGE_LIMIT = 10L * 1024 * 1024;
        long OTHER_LIMIT = 50L * 1024 * 1024;

        if (contentType != null && contentType.startsWith("image/")) {
            if (size > IMAGE_LIMIT) return ResponseEntity.status(413).body("Image too large (max 10MB)");
        } else {
            if (size > OTHER_LIMIT) return ResponseEntity.status(413).body("File too large (max 50MB)");
        }

        try {
            Map<String, String> result = storageService.uploadFile(folder, file);
            Map<String, Object> resp = new HashMap<>();
            resp.put("url", result.get("url"));
            resp.put("path", result.get("path"));
            resp.put("name", result.get("name"));
            resp.put("contentType", result.get("contentType"));
            resp.put("size", size);
            return ResponseEntity.ok(resp);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    // Obtener cantidad de mensajes no leídos en una conversación
    @GetMapping("/unread/conversation/{convId}/{userId}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable String convId, @PathVariable String userId) {
        long count = messageService.getUnreadCountForConversation(convId, userId);
        return ResponseEntity.ok(count);
    }

    // Obtener cantidad de conversaciones con mensajes pendientes para un usuario
    @GetMapping("/unread/user/{userId}")
    public ResponseEntity<Long> getPendingConversations(@PathVariable String userId) {
        long count = messageService.getPendingConversationsCountForUser(userId);
        return ResponseEntity.ok(count);
    }

    // Marcar conversación como leída
    @PostMapping("/mark-read/{convId}/{userId}")
    public ResponseEntity<Void> markRead(@PathVariable String convId, @PathVariable String userId) {
        messageService.markConversationRead(convId, userId);
        return ResponseEntity.ok().build();
    }
}