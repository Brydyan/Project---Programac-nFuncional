package ec.edu.upse.backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.MessageEntity;

public interface MessageRepository extends MongoRepository<MessageEntity, String>{
    List<MessageEntity> findBySenderId(String senderId);
    List<MessageEntity> findByReceiverId(String receiverId);
    List<MessageEntity> findByChannelId(String channelId);
}
