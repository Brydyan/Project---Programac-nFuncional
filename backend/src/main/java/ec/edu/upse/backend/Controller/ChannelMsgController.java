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

import ec.edu.upse.backend.Entity.ChannelMsgEntity;
import ec.edu.upse.backend.Service.ChannelMsgService;

@RestController
@RequestMapping("/app/v1/channel-messages")
public class ChannelMsgController {
    @Autowired
    private ChannelMsgService channelMsgService;

    // CREATE
    @PostMapping
    public ResponseEntity<ChannelMsgEntity> create(@RequestBody ChannelMsgEntity message) {
        return ResponseEntity.ok(channelMsgService.save(message));
    }

    // READ
    @GetMapping
    public ResponseEntity<List<ChannelMsgEntity>> getAll() {
        return ResponseEntity.ok(channelMsgService.getAllMessages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChannelMsgEntity> getById(@PathVariable String id) {
        return channelMsgService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/message-id/{messageId}")
    public ResponseEntity<ChannelMsgEntity> getByMessageId(@PathVariable String messageId) {
        return channelMsgService.getMessageByMessageId(messageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<ChannelMsgEntity>> getByChannel(@PathVariable String channelId) {
        return ResponseEntity.ok(channelMsgService.getMessagesByChannel(channelId));
    }

    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<ChannelMsgEntity>> getBySender(@PathVariable String senderId) {
        return ResponseEntity.ok(channelMsgService.getMessagesBySender(senderId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ChannelMsgEntity>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(channelMsgService.getMessagesByStatus(status));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ChannelMsgEntity> update(@PathVariable String id, @RequestBody ChannelMsgEntity newData) {
        ChannelMsgEntity updated = channelMsgService.updateMessage(id, newData);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = channelMsgService.deleteMessage(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
