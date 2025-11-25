package ec.edu.upse.backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.ChannelEntity;

public interface ChannelRepository extends MongoRepository<ChannelEntity,String>{
    List<ChannelEntity> findByOwnerId(String ownerId);
    List<ChannelEntity> findByType(String type);

    List<ChannelEntity> findByTypeAndNameContainingIgnoreCase(String type, String name);
    List<ChannelEntity> findByMembersContaining(String userId);
}