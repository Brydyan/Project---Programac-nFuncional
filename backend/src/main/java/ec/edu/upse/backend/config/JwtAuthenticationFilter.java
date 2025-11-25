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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

String path = request.getRequestURI();

// Excluir el endpoint de verificación
if (path.contains("/app/v1/auth/verify-password")) {
    filterChain.doFilter(request, response);
    return;
}


        // 1) Intentar leer token del header Authorization
        String header = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // 2) Si no viene en header (caso sendBeacon / beforeunload), revisar query param ?token=
        if (!StringUtils.hasText(token)) {
            String tokenParam = request.getParameter("token");
            if (StringUtils.hasText(tokenParam)) {
                token = tokenParam;
            }
        }

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            DecodedJWT decoded = jwtUtil.validateToken(token);

            // Validate session in DB
            var opt = sessionService.validateSession(token);
            if (opt.isPresent()) {
                String userId = jwtUtil.getUserIdFromToken(decoded);
                String username = jwtUtil.getUsernameFromToken(decoded);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.emptyList()
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (JWTVerificationException ex) {
            // Invalid token -> do not set authentication
        } catch (Exception ex) {
            // Any other error -> do not set authentication
        }

        filterChain.doFilter(request, response);
    }
}
