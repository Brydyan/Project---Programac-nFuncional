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

import ec.edu.upse.backend.Entity.SessionEntity;
import ec.edu.upse.backend.Service.SessionService;

@RestController
@RequestMapping("/app/v1/sessions")
public class SessionController {
    @Autowired
    private SessionService sessionService;

    // CREATE
    @PostMapping
    public ResponseEntity<SessionEntity> create(@RequestBody SessionEntity session) {
        return ResponseEntity.ok(sessionService.save(session));
    }

    // READ
    @GetMapping
    public ResponseEntity<List<SessionEntity>> getAll() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionEntity> getById(@PathVariable String id) {
        return sessionService.getSessionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<SessionEntity> getByToken(@PathVariable String token) {
        return sessionService.getSessionByToken(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionEntity>> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(sessionService.getSessionsByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SessionEntity>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(sessionService.getSessionsByStatus(status));
    }

    @GetMapping("/device/{device}")
    public ResponseEntity<List<SessionEntity>> getByDevice(@PathVariable String device) {
        return ResponseEntity.ok(sessionService.getSessionsByDevice(device));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<SessionEntity> update(@PathVariable String id, @RequestBody SessionEntity newData) {
        SessionEntity updated = sessionService.updateSession(id, newData);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = sessionService.deleteSession(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // DELETE all sessions for a user
    @DeleteMapping("/user/{userId}/all")
    public ResponseEntity<Void> deleteAllByUserId(@PathVariable String userId) {
        sessionService.deleteSessionsByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
