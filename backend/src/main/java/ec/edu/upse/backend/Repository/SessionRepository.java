package ec.edu.upse.backend.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ec.edu.upse.backend.Entity.SessionEntity;

@Repository
public interface SessionRepository extends MongoRepository<SessionEntity, String> {

    // Buscar por campos clave
    Optional<SessionEntity> findBySessionId(String sessionId);

    Optional<SessionEntity> findByToken(String token);

    List<SessionEntity> findByUserId(String userId);

    List<SessionEntity> findByStatus(String status);

    // Sesiones activas del usuario
    List<SessionEntity> findByUserIdAndStatus(String userId, String status);

    // Sesión válida (token OK + no está expirada)
    Optional<SessionEntity> findByTokenAndValidTrue(String token);

    // Sesiones no expiradas
    List<SessionEntity> findByExpiresAtAfter(Instant now);

    // Sesiones de un dispositivo específico
    List<SessionEntity> findByUserIdAndDevice(String userId, String device);

    // Sesiones por navegador
    List<SessionEntity> findByBrowser(String browser);

    // Filtrar por IP
    List<SessionEntity> findByIpAddress(String ip);

    // Ver si una sesión sigue activa y válida
    Optional<SessionEntity> findBySessionIdAndValidTrue(String sessionId);
}
