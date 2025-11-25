package ec.edu.upse.backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.MessageEntity;
import ec.edu.upse.backend.Entity.MessageEntity.MessageStatus;

public interface MessageRepository extends MongoRepository<MessageEntity, String>{
    List<MessageEntity> findBySenderId(String senderId);
    List<MessageEntity> findByReceiverId(String receiverId);
    List<MessageEntity> findByChannelId(String channelId);

    // count unread/delivered/read states
    long countByChannelIdAndStatus(String channelId, MessageStatus status);
    long countByReceiverIdAndStatus(String receiverId, MessageStatus status);

    List<MessageEntity> findByChannelIdAndStatus(String channelId, MessageStatus status);
    List<MessageEntity> findByReceiverIdAndStatus(String receiverId, MessageStatus status);

    MessageEntity findTopBySenderIdAndReceiverIdOrderByTimestampDesc(String senderId, String receiverId);

    List<MessageEntity> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
            String senderId1, String receiverId1,
            String senderId2, String receiverId2
    );

}