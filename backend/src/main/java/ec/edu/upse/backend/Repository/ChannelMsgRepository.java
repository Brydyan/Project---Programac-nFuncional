package ec.edu.upse.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.ChannelMsgEntity;

public interface ChannelMsgRepository extends MongoRepository<ChannelMsgEntity, String> {
    List<ChannelMsgEntity> findByChannel(String channelId);
    List<ChannelMsgEntity> findBySenderId(String senderId);
    Optional<ChannelMsgEntity> findByMessageId(String messageId);
    List<ChannelMsgEntity> findByStatus(String status);
}
