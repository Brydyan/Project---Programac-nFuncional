package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // CREATE
    public UserEntity save(UserEntity user) {
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
            existing.setUsername(newUser.getUsername());
            existing.setDisplayName(newUser.getDisplayName());
            existing.setEmail(newUser.getEmail());
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
