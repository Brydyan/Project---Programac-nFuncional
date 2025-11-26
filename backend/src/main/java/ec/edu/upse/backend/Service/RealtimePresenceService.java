package ec.edu.upse.backend.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RealtimePresenceService {

    private final StringRedisTemplate redis;

    private static final String PREFIX = "presence:user:";
    // TTL por defecto 60 segundos -> presencia expira rápido si el cliente cierra el navegador
    private static final Duration TTL = Duration.ofSeconds(60);

    public void setOnline(String userId, String sessionId) {
        String key = PREFIX + userId + ":session:" + sessionId;
        redis.opsForValue().set(key, "ONLINE", TTL);
    }

    public void refresh(String userId, String sessionId) {
        String key = PREFIX + userId + ":session:" + sessionId;
        redis.expire(key, TTL);
    }

    public void setOffline(String userId, String sessionId) {
        String key = PREFIX + userId + ":session:" + sessionId;
        redis.delete(key);
    }

    public void setInactive(String userId, String sessionId) {
        String key = PREFIX + userId + ":session:" + sessionId;
        redis.opsForValue().set(key, "INACTIVE", TTL);
    }

    public boolean isUserOnline(String userId) {
        Set<String> keys = redis.keys(PREFIX + userId + ":session:*");
        return keys != null && !keys.isEmpty();
    }

    public Map<String, String> getUserSessionsStatus(String userId) {
        Set<String> keys = redis.keys(PREFIX + userId + ":session:*");
        Map<String, String> result = new HashMap<>();

        if (keys != null) {
            keys.forEach(k -> result.put(k, redis.opsForValue().get(k)));
        }

        return result;
    }
}
