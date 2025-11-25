package ec.edu.upse.backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.NotificationEntity;

public interface NotificationRepository extends MongoRepository<NotificationEntity, String>{
    List<NotificationEntity> findByUserId(String userId);

}
