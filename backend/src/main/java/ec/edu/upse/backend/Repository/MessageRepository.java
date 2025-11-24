package ec.edu.upse.backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.MessageEntity;

public interface MessageRepository extends MongoRepository<MessageEntity, String>{
    List<MessageEntity> findBySenderId(String senderId);
    List<MessageEntity> findByReceiverId(String receiverId);
    List<MessageEntity> findByChannelId(String channelId);

    MessageEntity findTopBySenderIdAndReceiverIdOrderByTimestampDesc(String senderId, String receiverId);

    List<MessageEntity> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
            String senderId1, String receiverId1,
            String senderId2, String receiverId2
    );

}