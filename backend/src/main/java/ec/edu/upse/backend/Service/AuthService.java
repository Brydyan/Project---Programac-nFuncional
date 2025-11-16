package ec.edu.upse.backend.Service;

import java.time.Instant;

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

    public AuthResponse login(LoginRequest req, HttpServletRequest request) {

        // Buscar usuario
        UserEntity user = userService.findByIdentifier(req.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        boolean ok;

        if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$")) {
            ok = encoder.matches(req.getPassword(), user.getPassword());
        } else {
            ok = user.getPassword().equals(req.getPassword());
        }

        if (!ok) throw new RuntimeException("Usuario o contraseña incorrectos");

        // Generar JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // Datos del request
        String ip = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");

        // Crear sesión en Mongo + Redis
        sessionService.createSession(
                user.getId(),
                token,
                "WEB",
                ip,
                browser,
                Instant.now().plusSeconds(3600 * 24)
        );

        // Respuesta para frontend
        return new AuthResponse(token, user.getId(), user.getUsername());
    }
}
