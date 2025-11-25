package ec.edu.upse.backend.Controller;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.upse.backend.Entity.SessionEntity;
import ec.edu.upse.backend.Service.SessionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/app/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService service;
    private static final Logger log = LoggerFactory.getLogger(SessionController.class);

    // ============================
    // CREAR SESIÓN (Login)
    // ============================
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody SessionEntity s) {
        SessionEntity entity = service.createSession(
                s.getUserId(),
                s.getToken(),
                s.getDevice(),
                s.getIpAddress(),
                s.getBrowser(),
                Instant.now().plus(Duration.ofDays(7)) // expiración JWT simulada
        );
        return ResponseEntity.ok(entity);
    }

    // ============================
    // REFRESCAR ACTIVIDAD
    // ============================
    @PostMapping("/refresh/{sessionId}")
    public ResponseEntity<Void> refresh(@PathVariable String sessionId) {
        service.refreshActivity(sessionId);
        return ResponseEntity.ok().build();
    }

    // ============================
    // MARCAR ONLINE / INACTIVE (invocado desde frontend)
    // ============================
    @PostMapping("/online/{sessionId}")
    public ResponseEntity<Void> markOnline(@PathVariable String sessionId) {
        service.markOnlineBySessionId(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inactive/{sessionId}")
    public ResponseEntity<Void> markInactive(@PathVariable String sessionId) {
        service.markInactiveBySessionId(sessionId);
        return ResponseEntity.ok().build();
    }

    // ============================
    // LOGOUT SOLO UNA SESIÓN
    // ============================
    @PostMapping("/logout/{sessionId}")
    public ResponseEntity<Void> logout(@PathVariable String sessionId, jakarta.servlet.http.HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        String remote = request.getRemoteAddr();
        String authShort = auth == null ? "<none>" : (auth.length() > 20 ? auth.substring(0,10) + "..." : auth);
        log.info("logout called for sessionId={} from remote={} authShort={}", sessionId, remote, authShort);
        service.logout(sessionId);
        return ResponseEntity.ok().build();
    }

    // ============================
    // LOGOUT TODAS LAS SESIONES DE UN USUARIO
    // ============================
    @PostMapping("/logout/user/{userId}")
    public ResponseEntity<Void> logoutAll(@PathVariable String userId) {
        service.logoutAll(userId);
        return ResponseEntity.ok().build();
    }

    // ============================
    // GETTERS
    // ============================
    @GetMapping
    public ResponseEntity<List<SessionEntity>> all() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionEntity> getById(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<SessionEntity> getByToken(@PathVariable String token) {
        // registrar petición para diagnóstico (no imprimir token completo por seguridad)
        String tokenShort = token == null ? "<null>" : (token.length() > 8 ? token.substring(0,4) + "..." + token.substring(token.length()-4) : token);
        log.debug("getByToken called tokenShort={}", tokenShort);
        return service.getByToken(token)
                .map(s -> {
                    log.debug("getByToken: found session sessionId={} userId={} valid={}", s.getSessionId(), s.getUserId(), s.isValid());
                    return ResponseEntity.ok(s);
                })
                .orElseGet(() -> {
                    log.debug("getByToken: no session found for tokenShort={}", tokenShort);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionEntity>> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }
}
