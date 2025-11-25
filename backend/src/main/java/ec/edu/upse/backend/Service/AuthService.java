package ec.edu.upse.backend.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Util.JwtUtil;
import ec.edu.upse.backend.dto.AuthResponse;
import ec.edu.upse.backend.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse login(LoginRequest req, HttpServletRequest request) {

        // Buscar usuario
        UserEntity user = userService.findByIdentifier(req.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        boolean ok = user.getPassword().startsWith("$2a$")
                || user.getPassword().startsWith("$2b$")
                ? encoder.matches(req.getPassword(), user.getPassword())
                : user.getPassword().equals(req.getPassword());

        if (!ok) throw new RuntimeException("Usuario o contraseña incorrectos");

        // Generar JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

                // Datos del request: obtener IP real del cliente usando cabeceras proxy si están presentes
                String ip = null;
                String xff = request.getHeader("X-Forwarded-For");
                if (xff != null && !xff.isBlank()) {
                        // X-Forwarded-For puede contener múltiples IPs separadas por coma, la primera es el cliente
                        ip = xff.split(",")[0].trim();
                }
                if (ip == null || ip.isBlank()) {
                        String xri = request.getHeader("X-Real-IP");
                        if (xri != null && !xri.isBlank()) ip = xri.trim();
                }
                if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();

                String browser = request.getHeader("User-Agent");

        // Crear sesión y activar presencia
        // Create a session without an expiration (persistent) — will be invalidated only on logout
        var session = sessionService.createSession(
                user.getId(),
                token,
                "WEB",
                ip,
                browser,
                null
        );

        // Devolver también la IP y la localización (para depuración y confirmación)
        return new ec.edu.upse.backend.dto.AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                session.getIpAddress(),
                session.getLocation()
        );
    }

    // Verificar credenciales sin crear sesión ni generar token
        public boolean verifyPassword(LoginRequest req) {

        UserEntity user = userService.findByIdentifier(req.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        boolean ok = user.getPassword().startsWith("$2a$")
                || user.getPassword().startsWith("$2b$")
                ? encoder.matches(req.getPassword(), user.getPassword())
                : user.getPassword().equals(req.getPassword());

        if (!ok) throw new RuntimeException("Usuario o contraseña incorrectos");

        return true;
        }

        // Verificar credenciales por userId (no crea sesión)
        public boolean verifyPasswordById(String userId, String password) {
                var opt = userService.getUserById(userId);
                if (opt.isEmpty()) {
                        log.warn("verifyPasswordById: userId not found={}", userId);
                        throw new RuntimeException("Usuario o contraseña incorrectos");
                }
                UserEntity user = opt.get();

                boolean ok;
                try {
                        if (user.getPassword() != null && (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$"))) {
                                ok = encoder.matches(password, user.getPassword());
                        } else {
                                ok = user.getPassword() != null && user.getPassword().equals(password);
                        }
                } catch (Exception ex) {
                        log.error("verifyPasswordById: error comparing password for userId={}", userId, ex);
                        throw new RuntimeException("Usuario o contraseña incorrectos");
                }

                log.debug("verifyPasswordById: userId={} passwordMatch={}", userId, ok);
                if (!ok) {
                        throw new RuntimeException("Usuario o contraseña incorrectos");
                }

                return true;
        }

}