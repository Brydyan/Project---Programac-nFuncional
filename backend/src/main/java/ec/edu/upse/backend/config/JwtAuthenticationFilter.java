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
        System.out.println("[JWT] Request path = " + path);

        // ... tus if de rutas públicas igual

        String header = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (!StringUtils.hasText(token)) {
            String tokenParam = request.getParameter("token");
            if (StringUtils.hasText(tokenParam)) {
                token = tokenParam;
            }
        }

        System.out.println("[JWT] Token recibido = " + token);

        if (!StringUtils.hasText(token)) {
            System.out.println("[JWT] Sin token → sigo sin auth");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            DecodedJWT decoded = jwtUtil.validateToken(token);
            System.out.println("[JWT] Token OK, sub = " + decoded.getSubject());

            var opt = sessionService.validateSession(token);
            System.out.println("[JWT] validateSession(token) presente? " + opt.isPresent());

            if (opt.isPresent()) {
                String userId = jwtUtil.getUserIdFromToken(decoded);
                System.out.println("[JWT] Autenticando userId = " + userId);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList()
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                System.out.println("[JWT] Sesión NO válida, no autentico");
            }

        } catch (JWTVerificationException ex) {
            System.out.println("[JWT] Token inválido: " + ex.getMessage());
            // no autenticamos, pero tampoco rompemos
        } catch (Exception ex) {
            System.out.println("[JWT] Error inesperado: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

}
