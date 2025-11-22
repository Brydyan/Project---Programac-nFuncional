package ec.edu.upse.backend.Service;

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

        boolean ok = user.getPassword().startsWith("$2a$")
                || user.getPassword().startsWith("$2b$")
                ? encoder.matches(req.getPassword(), user.getPassword())
                : user.getPassword().equals(req.getPassword());

        if (!ok) throw new RuntimeException("Usuario o contraseña incorrectos");

        // Generar JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // Datos del request
        String ip = request.getRemoteAddr();
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

        return new AuthResponse(token, user.getId(), user.getUsername());
    }
}