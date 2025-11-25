package ec.edu.upse.backend.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import ec.edu.upse.backend.Service.SessionService;
import ec.edu.upse.backend.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SessionService sessionService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, SessionService sessionService) {
        this.jwtUtil = jwtUtil;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🟦 REGLA ESPECIAL: endpoint de verificación de contraseña
        if (path.contains("/app/v1/auth/verify-password")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔓 RUTAS PÚBLICAS (no procesan JWT)
        if (path.startsWith("/app/v1/auth/")
            || path.startsWith("/app/v1/user/available/")
            || path.startsWith("/app/v1/sessions/token/")
            || path.startsWith("/app/v1/sessions/refresh/")
            || path.startsWith("/app/v1/user/token/")
            || path.startsWith("/ws/")) {

            filterChain.doFilter(request, response);
            return;
        }

        // Intentar leer token del header Authorization
        String header = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // Si no viene en header → revisar query param ?token=
        if (!StringUtils.hasText(token)) {
            String tokenParam = request.getParameter("token");
            if (StringUtils.hasText(tokenParam)) {
                token = tokenParam;
            }
        }

        // Si NO hay token → seguir sin autenticar
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            DecodedJWT decoded = jwtUtil.validateToken(token);

            var opt = sessionService.validateSession(token);
            if (opt.isPresent()) {
                String username = jwtUtil.getUsernameFromToken(decoded);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.emptyList()
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (JWTVerificationException ex) {
            // token inválido → no autenticamos
        } catch (Exception ex) {
            // no rompemos la request
        }

        filterChain.doFilter(request, response);
    }
}
