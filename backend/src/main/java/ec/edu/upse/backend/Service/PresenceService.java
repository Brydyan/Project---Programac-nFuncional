package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Domain.PresenceValidator;
import ec.edu.upse.backend.Entity.PresenceEntity;
import ec.edu.upse.backend.Repository.PresenceRepository;

@Service
public class PresenceService {
    @Autowired
    private PresenceRepository presenceRepository;

    public PresenceEntity save(PresenceEntity presence) {
        if (!PresenceValidator.esUserIdValido(presence.getUserId())) {
            throw new IllegalArgumentException("UserId inválido para presencia");
        }

        String normalizedStatus = PresenceValidator.normalizarStatus(presence.getStatus());
        if (normalizedStatus == null) {
            throw new IllegalArgumentException("Status de presencia inválido");
        }

        presence.setStatus(normalizedStatus);
        // lastSeen ya se setea por defecto en la entidad

        return presenceRepository.save(presence);
    }

    public List<PresenceEntity> getAll() {
        return presenceRepository.findAll();
    }

    public Optional<PresenceEntity> getById(String id) {
        return presenceRepository.findById(id);
    }

    public List<PresenceEntity> getByUser(String userId) {
        return presenceRepository.findByUserId(userId);
    }

    public PresenceEntity updateStatus(String id, String newStatus) {
        Optional<PresenceEntity> aux = presenceRepository.findById(id);
        if (aux.isPresent()) {
            String normalized = PresenceValidator.normalizarStatus(newStatus);
            if (normalized == null) {
                throw new IllegalArgumentException("Status de presencia inválido");
            }
            PresenceEntity presence = aux.get();
            presence.setStatus(normalized);
            presence.setLastSeen(java.time.Instant.now());
            return presenceRepository.save(presence);
        }
        return null;
    }

    public boolean delete(String id) {
        if (presenceRepository.existsById(id)) {
            presenceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
