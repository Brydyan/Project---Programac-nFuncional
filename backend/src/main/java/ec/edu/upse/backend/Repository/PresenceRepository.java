package ec.edu.upse.backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.PresenceEntity;

public interface PresenceRepository extends MongoRepository<PresenceEntity, String>{
    List<PresenceEntity> findByUserId(String userId);
}
