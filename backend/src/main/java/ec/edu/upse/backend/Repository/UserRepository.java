package ec.edu.upse.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ec.edu.upse.backend.Entity.UserEntity;

public interface UserRepository extends MongoRepository<UserEntity, String>{
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);

    //buscar un usuario por su username o alias, es para poder recuperrarlo en el modal de nueva conversacion
    List<UserEntity> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
            String username,
            String displayName
    );
}
