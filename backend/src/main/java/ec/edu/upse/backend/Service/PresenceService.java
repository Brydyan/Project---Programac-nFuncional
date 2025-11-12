package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.PresenceEntity;
import ec.edu.upse.backend.Repository.PresenceRepository;

@Service
public class PresenceService {
    @Autowired
    private PresenceRepository presenceRepository;

    public PresenceEntity save(PresenceEntity presence) {
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

    public boolean delete(String id) {
        if (presenceRepository.existsById(id)) {
            presenceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
