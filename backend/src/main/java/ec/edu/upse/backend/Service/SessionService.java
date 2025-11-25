package ec.edu.upse.backend.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.upse.backend.Entity.SessionEntity;
import ec.edu.upse.backend.Repository.SessionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository repo;
    private final RealtimePresenceService presence;
    private final IpLocationService ipLocationService;

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
        // Intentar resolver ubicación a partir de la IP (ipapi.co)
        try {
            String loc = ipLocationService.getLocation(ip);
            s.setLocation(loc);
        } catch (Exception e) {
            s.setLocation(null);
        }
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
        if (sessionId == null) {
            log.warn("refreshActivity called with null sessionId");
            return;
        }
        log.debug("refreshActivity called for sessionId={}", sessionId);
        repo.findBySessionId(sessionId).ifPresentOrElse(s -> {
            // Si tiene fecha de expiración y ya expiró → invalidar automáticamente
            if (s.getExpiresAt() != null && s.getExpiresAt().isBefore(Instant.now())) {
                log.info("refreshActivity: session expired -> invalidating sessionId={} userId={}", s.getSessionId(), s.getUserId());
                invalidateSession(s);
                return;
            }

            s.setLastActivity(Instant.now());
            repo.save(s);

            // Mantener presencia en Redis
            presence.refresh(s.getUserId(), s.getSessionId());
            log.debug("refreshActivity: refreshed sessionId={} userId={}", s.getSessionId(), s.getUserId());
        }, () -> {
            log.debug("refreshActivity: no session found for sessionId={}", sessionId);
        });
    }

    // ==================================================
    // LOGOUT (solo una sesión)
    // ==================================================
    public void logout(String sessionIdOrId) {
        // Intentamos por sessionId (valor público) y si no existe, por _id interno
        repo.findBySessionId(sessionIdOrId).ifPresentOrElse(s -> {
            log.info("logout: invalidating by sessionId={}, userId={}", s.getSessionId(), s.getUserId());
            invalidateSession(s);
        }, () -> {
            // intentar buscar por id interno (por si el cliente envía el campo equivocado)
            repo.findById(sessionIdOrId).ifPresent(s2 -> {
                log.info("logout: invalidating by internal id={}, resolved sessionId={}, userId={}", sessionIdOrId, s2.getSessionId(), s2.getUserId());
                invalidateSession(s2);
            });
        });
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
        try {
            String sid = s.getSessionId();
            String uid = s.getUserId();
            log.debug("invalidateSession: before -> sessionId={}, userId={}, valid={}, status={}", sid, uid, s.isValid(), s.getStatus());
            s.setValid(false);
            s.setStatus("inactive");
            repo.save(s);
            presence.setOffline(uid, sid);
            log.info("invalidateSession: after -> sessionId={}, userId={} marked invalid/inactive", sid, uid);
        } catch (Exception e) {
            log.error("invalidateSession: error invalidating session id={} : {}", s == null ? "<null>" : s.getSessionId(), e.getMessage(), e);
        }
    }
}
