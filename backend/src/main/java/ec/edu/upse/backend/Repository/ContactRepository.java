package ec.edu.upse.backend.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.ContactEntity;

public interface ContactRepository extends MongoRepository<ContactEntity, String>{
    List<ContactEntity> findByUserId(String userId);
    List<ContactEntity> findByContactId(String contactId);
}
