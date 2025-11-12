package ec.edu.upse.backend.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.upse.backend.Entity.NotificationEntity;
import ec.edu.upse.backend.Service.NotificationService;

@RestController
@RequestMapping("/app/v1/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @PostMapping
    public ResponseEntity<NotificationEntity> create(@RequestBody NotificationEntity notification) {
        return ResponseEntity.ok(notificationService.save(notification));
    }
    @GetMapping
    public ResponseEntity<List<NotificationEntity>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<NotificationEntity> getById(@PathVariable String id) {
        return notificationService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("user/{userId}")
    public ResponseEntity<List<NotificationEntity>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getByUser(userId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = notificationService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
