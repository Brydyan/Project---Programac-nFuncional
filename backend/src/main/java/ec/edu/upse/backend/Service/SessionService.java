package ec.edu.upse.backend.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.SessionEntity;
import ec.edu.upse.backend.Repository.SessionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository repo;
    private final RealtimePresenceService presence;

    // CREATE sesión
    public SessionEntity createSession(String userId, String token, String device,
                                       String ip, String browser, Instant expiresAt) {

        SessionEntity s = new SessionEntity();
        s.setSessionId(UUID.randomUUID().toString());
        s.setUserId(userId);
        s.setToken(token);
        s.setDevice(device);
        s.setIpAddress(ip);
        s.setBrowser(browser);
        s.setStatus("active");
        s.setExpiresAt(expiresAt);
        s.setValid(true);

        repo.save(s);

        // Activar presencia en Redis
        presence.setOnline(userId, s.getSessionId());

        return s;
    }

    // REFRESH
    public void refreshActivity(String sessionId) {
        Optional<SessionEntity> opt = repo.findById(sessionId);
        if (opt.isPresent()) {
            SessionEntity s = opt.get();
            s.setLastActivity(Instant.now());
            repo.save(s);

            // Actualizar presencia
            presence.refresh(s.getUserId(), s.getSessionId());
        }
    }

    // LOGOUT sesión
    public void logout(String sessionId) {
        Optional<SessionEntity> opt = repo.findById(sessionId);
        if (opt.isPresent()) {
            SessionEntity s = opt.get();
            s.setValid(false);
            s.setStatus("inactive");
            repo.save(s);

            // Quitar presencia
            presence.setOffline(s.getUserId(), s.getSessionId());
        }
    }

    // LOGOUT GLOBAL
    public void logoutAll(String userId) {
        List<SessionEntity> list = repo.findByUserId(userId);
        for (SessionEntity s : list) logout(s.getId());
    }

    // Listados
    public List<SessionEntity> getAll() { return repo.findAll(); }
    public Optional<SessionEntity> getById(String id) { return repo.findById(id); }
    public List<SessionEntity> getByUserId(String u) { return repo.findByUserId(u); }
    public Optional<SessionEntity> getByToken(String t) { return repo.findByToken(t); }
}

