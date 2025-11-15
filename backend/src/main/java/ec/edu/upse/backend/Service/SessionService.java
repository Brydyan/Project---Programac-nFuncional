package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Domain.SessionValidator;
import ec.edu.upse.backend.Entity.SessionEntity;
import ec.edu.upse.backend.Repository.SessionRepository;

@Service
public class SessionService {
    @Autowired
    private SessionRepository sessionRepository;

    // CREATE
    public SessionEntity save(SessionEntity session) {
        if (!SessionValidator.esUserIdValido(session.getUserId())) {
            throw new IllegalArgumentException("UserId inválido para sesión");
        }
        if (!SessionValidator.esTokenValido(session.getToken())) {
            throw new IllegalArgumentException("Token inválido para sesión");
        }

        String normalizedStatus = SessionValidator.normalizarStatus(session.getStatus());
        if (normalizedStatus == null) {
            throw new IllegalArgumentException("Status de sesión inválido");
        }

        session.setStatus(normalizedStatus);
        // horaFecha ya se setea por defecto en la entidad

        return sessionRepository.save(session);
    }

    // READ
    public List<SessionEntity> getAllSessions() {
        return sessionRepository.findAll();
    }

    public Optional<SessionEntity> getSessionById(String id) {
        return sessionRepository.findById(id);
    }

    public Optional<SessionEntity> getSessionByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    public List<SessionEntity> getSessionsByUserId(String userId) {
        return sessionRepository.findByUserId(userId);
    }

    public List<SessionEntity> getSessionsByStatus(String status) {
        return sessionRepository.findByStatus(status);
    }

    public List<SessionEntity> getSessionsByDevice(String device) {
        return sessionRepository.findByDevice(device);
    }

    // UPDATE
    public SessionEntity updateSession(String id, SessionEntity newData) {
        Optional<SessionEntity> aux = sessionRepository.findById(id);
        if (aux.isPresent()) {
            SessionEntity session = aux.get();

            String normalizedStatus = SessionValidator.normalizarStatus(newData.getStatus());
            if (normalizedStatus == null) {
                throw new IllegalArgumentException("Status de sesión inválido");
            }

            session.setStatus(normalizedStatus);
            session.setDevice(newData.getDevice());
            session.setIpAddress(newData.getIpAddress());
            session.setLocation(newData.getLocation());
            session.setBrowser(newData.getBrowser());
            // horaFecha podrías actualizarla si consideras que cambia al modificar
            return sessionRepository.save(session);
        }
        return null;
    }

    // DELETE
    public boolean deleteSession(String id) {
        if (sessionRepository.existsById(id)) {
            sessionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // DELETE all sessions by user
    public void deleteSessionsByUserId(String userId) {
        List<SessionEntity> sessions = sessionRepository.findByUserId(userId);
        sessionRepository.deleteAll(sessions);
    }
}
