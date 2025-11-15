package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Domain.UserValidator;
import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // CREATE
    public UserEntity save(UserEntity user) {
        String normalizedUsername = UserValidator.normalizarUsername(user.getUsername());
        String normalizedEmail = UserValidator.normalizarEmail(user.getEmail());

        if (normalizedUsername == null) {
            throw new IllegalArgumentException("Username inválido");
        }
        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Email inválido");
        }

        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);

        return userRepository.save(user);
    }

    // READ
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // UPDATE
    public UserEntity updateUser(String id, UserEntity newUser) {
        Optional<UserEntity> aux = userRepository.findById(id);
        if (aux.isPresent()) {
            UserEntity existing = aux.get();

            // ✅ validamos y normalizamos username
            String normalizedUsername = UserValidator.normalizarUsername(newUser.getUsername());
            if (normalizedUsername == null) {
                throw new IllegalArgumentException("Username inválido");
            }

            // ✅ validamos y normalizamos email
            String normalizedEmail = UserValidator.normalizarEmail(newUser.getEmail());
            if (normalizedEmail == null) {
                throw new IllegalArgumentException("Email inválido");
            }

            existing.setUsername(normalizedUsername);
            existing.setDisplayName(newUser.getDisplayName());
            existing.setEmail(normalizedEmail);
            existing.setStatus(newUser.getStatus());
            existing.setPreferences(newUser.getPreferences());

            return userRepository.save(existing);
        }
        return null;
    }

    // DELETE
    public boolean deleteUser(String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
