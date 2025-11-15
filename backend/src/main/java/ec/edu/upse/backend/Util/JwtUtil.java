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

    @Value("${jwt.expiration-hours:24}")
    private int expirationHours;

    public String generateToken(String userId, String username) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(now.plusSeconds(expirationHours * 3600L));

        return JWT.create()
                .withSubject(userId)
                .withClaim("username", username)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }
}
