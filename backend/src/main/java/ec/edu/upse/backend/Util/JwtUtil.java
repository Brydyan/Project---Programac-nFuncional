package ec.edu.upse.backend.Util;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-hours:-1}")
    private int expirationHours;

    public String generateToken(String userId, String username) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);

        var builder = JWT.create()
            .withSubject(userId)
            .withClaim("username", username)
            .withIssuedAt(issuedAt);

        // If expirationHours is <= 0 we treat token as non-expiring (no exp claim)
        if (expirationHours > 0) {
            Date expiresAt = Date.from(now.plusSeconds(expirationHours * 3600L));
            builder = builder.withExpiresAt(expiresAt);
        }

        return builder.sign(algorithm);
    }
}
