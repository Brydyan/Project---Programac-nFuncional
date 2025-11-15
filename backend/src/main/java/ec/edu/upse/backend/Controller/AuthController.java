package ec.edu.upse.backend.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Service.UserService;
import ec.edu.upse.backend.Util.JwtUtil;
import ec.edu.upse.backend.dto.AuthResponse;
import ec.edu.upse.backend.dto.LoginRequest;

@RestController
@RequestMapping("/app/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String idOrEmail = req.getIdentifier();
        String password = req.getPassword();

        Optional<UserEntity> userOpt = userService.getUserByUsername(idOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userService.getUserByEmail(idOrEmail);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
        }

        UserEntity user = userOpt.get();
        String stored = user.getPassword();
        boolean ok = false;
        if (stored == null) {
            ok = false;
        } else if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
            // parece ser un hash bcrypt
            ok = passwordEncoder.matches(password, stored);
        } else {
            // comparación simple (si las contraseñas están en texto plano)
            ok = stored.equals(password);
        }

        if (!ok) {
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        AuthResponse resp = new AuthResponse(token, user.getId(), user.getUsername());
        return ResponseEntity.ok(resp);
    }
}
