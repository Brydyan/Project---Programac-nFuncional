package ec.edu.upse.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.SessionEntity;

public interface SessionRepository extends MongoRepository<SessionEntity, String> {
    List<SessionEntity> findByUserId(String userId);
    Optional<SessionEntity> findByToken(String token);
    List<SessionEntity> findByStatus(String status);
    List<SessionEntity> findByDevice(String device);
}
