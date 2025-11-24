package ec.edu.upse.backend.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.upse.backend.Entity.SessionEntity;
import ec.edu.upse.backend.Repository.SessionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository repo;
    private final RealtimePresenceService presence;

    // ==================================================
    // CREAR SESIÓN (Login)
    // ==================================================
    @Transactional
    public SessionEntity createSession(
            String userId,
            String token,
            String device,
            String ip,
            String browser,
            Instant expiresAt
    ) {

        // Cerrar sesiones vencidas del usuario automáticamente
        cleanupExpiredSessions(userId);

        SessionEntity s = new SessionEntity();
        s.setId(null); // generar automáticamente
        s.setSessionId(UUID.randomUUID().toString());
        s.setUserId(userId);
        s.setToken(token);
        s.setDevice(device);
        s.setIpAddress(ip);
        s.setBrowser(browser);
        s.setStatus("active");
        s.setValid(true);

        Instant now = Instant.now();
        s.setLoginAt(now);
        s.setLastActivity(now);
        s.setExpiresAt(expiresAt);

        repo.save(s);

        // Hacer visible al usuario en tiempo real
        presence.setOnline(userId, s.getSessionId());

        return s;
    }

    // ==================================================
    // REFRESCAR ACTIVIDAD (cada request válida)
    // ==================================================
    public void refreshActivity(String sessionId) {
        repo.findBySessionId(sessionId).ifPresent(s -> {
            // Si tiene fecha de expiración y ya expiró → invalidar automáticamente
            if (s.getExpiresAt() != null && s.getExpiresAt().isBefore(Instant.now())) {
                invalidateSession(s);
                return;
            }

            s.setLastActivity(Instant.now());
            repo.save(s);

            // Mantener presencia en Redis
            presence.refresh(s.getUserId(), s.getSessionId());
        });
    }

    // ==================================================
    // LOGOUT (solo una sesión)
    // ==================================================
    public void logout(String sessionId) {
        repo.findBySessionId(sessionId).ifPresent(this::invalidateSession);
    }

    // ==================================================
    // LOGOUT GLOBAL (cerrar todas)
    // ==================================================
    @Transactional
    public void logoutAll(String userId) {
        List<SessionEntity> list = repo.findByUserId(userId);
        for (SessionEntity s : list) invalidateSession(s);
    }

    // ==================================================
    // MARCAR SESIÓN COMO ACTIVA / INACTIVA (invocado por frontend)
    // ==================================================
    public void markOnlineBySessionId(String sessionId) {
        repo.findBySessionId(sessionId).ifPresent(s -> {
            s.setStatus("active");
            s.setValid(true);
            s.setLastActivity(Instant.now());
            repo.save(s);
            presence.setOnline(s.getUserId(), s.getSessionId());
        });
    }

    public void markInactiveBySessionId(String sessionId) {
        repo.findBySessionId(sessionId).ifPresent(s -> {
            s.setStatus("inactive");
            repo.save(s);
            presence.setInactive(s.getUserId(), s.getSessionId());
        });
    }

    // ==================================================
    // OBTENER SESIONES
    // ==================================================
    public List<SessionEntity> getAll() {
        return repo.findAll();
    }

    public Optional<SessionEntity> getById(String id) {
        return repo.findById(id);
    }

    public Optional<SessionEntity> getByToken(String token) {
        return repo.findByToken(token);
    }

    public List<SessionEntity> getByUserId(String userId) {
        return repo.findByUserId(userId);
    }

    // ==================================================
    // VALIDAR SESIÓN (para filtros de seguridad)
    // ==================================================
    public Optional<SessionEntity> validateSession(String token) {
        return repo.findByTokenAndValidTrue(token)
            // If expiresAt is null -> treat as non-expiring; otherwise check date
            .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(Instant.now()));
    }

    // ==================================================
    // LIMPIEZA DE SESIONES VENCIDAS
    // ==================================================
    public void cleanupExpiredSessions(String userId) {
        List<SessionEntity> sessions = repo.findByUserId(userId);

        Instant now = Instant.now();
        for (SessionEntity s : sessions) {
            if (s.getExpiresAt() != null && s.getExpiresAt().isBefore(now)) {
                invalidateSession(s);
            }
        }
    }

    // ==================================================
    // MÉTODO INTERNO: invalidar sesión
    // ==================================================
    private void invalidateSession(SessionEntity s) {
        s.setValid(false);
        s.setStatus("inactive");
        repo.save(s);

        presence.setOffline(s.getUserId(), s.getSessionId());
    }
}
