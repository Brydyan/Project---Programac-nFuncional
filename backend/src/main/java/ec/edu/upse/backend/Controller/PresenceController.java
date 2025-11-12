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

import ec.edu.upse.backend.Entity.PresenceEntity;
import ec.edu.upse.backend.Service.PresenceService;

@RestController
@RequestMapping("/app/v1/presence")
public class PresenceController {
    @Autowired
    private PresenceService presenceService;
    @PostMapping
    public ResponseEntity<PresenceEntity> create(@RequestBody PresenceEntity presence) {
        return ResponseEntity.ok(presenceService.save(presence));
    }
    @GetMapping
    public ResponseEntity<List<PresenceEntity>> getAll() {
        return ResponseEntity.ok(presenceService.getAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<PresenceEntity> getById(@PathVariable String id) {
        return presenceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PresenceEntity>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(presenceService.getByUser(userId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = presenceService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
