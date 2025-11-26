package ec.edu.upse.backend.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Domain.UserValidator;
import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Repository.UserRepository;
import ec.edu.upse.backend.dto.UserSummaryDto;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // CREATE
    public UserEntity save(UserEntity user) {
        // Mapear alias → username y nombre → displayName si es necesario
        user.processAliasAndNombre();
        
        String normalizedUsername = UserValidator.normalizarUsername(user.getUsername());
        String normalizedEmail = UserValidator.normalizarEmail(user.getEmail());

        if (normalizedUsername == null) {
            throw new IllegalArgumentException("Username inválido");
        }
        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Email inválido");
        }

        // Validar que username no esté duplicado
        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }
        
        // Validar que email no esté duplicado
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);

        // Validar y hashear la contraseña antes de guardar
        String plain = user.getPassword();
        if (plain == null || plain.trim().isEmpty()) {
            throw new IllegalArgumentException("Password inválida");
        }
        if (plain.length() < 8) {
            throw new IllegalArgumentException("Password demasiado corta (mínimo 8 caracteres)");
        }
        // Requerir al menos una mayúscula, un número y un carácter especial
        String pwRegex = ".*(?=.*[A-Z]).*"; // must contain uppercase
        String numRegex = ".*(?=.*\\d).*"; // must contain digit
        String specialRegex = ".*(?=.*[@$!%*?&\\-_.:,;#\\(\\)\\[\\]{}\\+\\=\\|\\/~`\\^\\\\]).*"; // allow many specials
        if (!plain.matches(pwRegex) || !plain.matches(numRegex) || !plain.matches(specialRegex)) {
            throw new IllegalArgumentException("Password débil: requiere mayúscula, número y carácter especial");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashed = encoder.encode(plain);
        user.setPassword(hashed);

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

    public Optional<UserEntity> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
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

            // Si se proporciona una nueva contraseña, hashearla antes de actualizar
            String newPlain = newUser.getPassword();
            if (newPlain != null && !newPlain.trim().isEmpty()) {
                if (newPlain.length() < 8) {
                    throw new IllegalArgumentException("Password demasiado corta (mínimo 8 caracteres)");
                }
                // Validación de fuerza similar a save()
                String pwRegex = ".*(?=.*[A-Z]).*"; // must contain uppercase
                String numRegex = ".*(?=.*\\d).*"; // must contain digit
                String specialRegex = ".*(?=.*[@$!%*?&\\-_.:,;#\\(\\)\\[\\]{}\\+\\=\\|\\/~`\\^\\\\]).*";
                if (!newPlain.matches(pwRegex) || !newPlain.matches(numRegex) || !newPlain.matches(specialRegex)) {
                    throw new IllegalArgumentException("Password débil: requiere mayúscula, número y carácter especial");
                }
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                existing.setPassword(encoder.encode(newPlain));
            }

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

    public Optional<UserEntity> findByIdentifier(String idOrEmail) {
        Optional<UserEntity> u = userRepository.findByUsername(idOrEmail);
        if (u.isEmpty())
            u = userRepository.findByEmail(idOrEmail);
        return u;
    }

    // AVAILABILITY CHECK
    public boolean isUsernameAvailable(String username) {
        String normalized = UserValidator.normalizarUsername(username);
        if (normalized == null) return false;
        return userRepository.findByUsername(normalized).isEmpty();
    }

    public boolean isEmailAvailable(String email) {
        String normalized = UserValidator.normalizarEmail(email);
        if (normalized == null) return false;
        return userRepository.findByEmail(normalized).isEmpty();
    }

    public List<UserSummaryDto> searchUsers(String query, String excludeUserId) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String q = query.trim();

        List<UserEntity> users = userRepository
                .findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(q, q);

        return users.stream()
                //para excluir al usuario actual de la busquedad
                .filter(u -> excludeUserId == null || !u.getId().equals(excludeUserId))
                .map(u -> new UserSummaryDto(
                        u.getId(),
                        u.getUsername(),
                        u.getDisplayName(),
                        u.getEmail(),
                        null 
                ))
                .collect(Collectors.toList());
    }

    public List<UserSummaryDto> getUsersByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        return ids.stream()
                .map(id -> userRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(u -> new UserSummaryDto(
                        u.getId(),
                        u.getUsername(),
                        u.getDisplayName(),
                        u.getEmail(),
                        null
                ))
                .collect(Collectors.toList());
    }

}