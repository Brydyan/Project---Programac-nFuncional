package ec.edu.upse.backend.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import ec.edu.upse.backend.Service.SessionService;
import ec.edu.upse.backend.Util.JwtUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil,
                                                           SessionService sessionService) {
        return new JwtAuthenticationFilter(jwtUtil, sessionService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // ===== PÚBLICAS =====
                .requestMatchers("/app/v1/auth/**").permitAll()
                .requestMatchers("/app/v1/sessions/token/**").permitAll()
                .requestMatchers("/app/v1/sessions/refresh/**").permitAll()
                .requestMatchers("/app/v1/user/token/**").permitAll()

                // WebSocket (handshake STOMP)
                .requestMatchers("/ws/**").permitAll()

                // ===== PRIVADAS =====
                .requestMatchers("/app/v1/user/**").authenticated()
                .requestMatchers("/app/v1/conversations/**").authenticated()
                .requestMatchers("/app/v1/messages/**").authenticated()

                // cualquier otra protegida
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ===== MODO DEV / DOCKER =====
        // Permitimos cualquier origen (LAN, localhost, etc.)
        // OJO: usamos AllowedOriginPatterns, no AllowedOrigins,
        // para que funcione bien con credenciales.
        config.setAllowedOriginPatterns(Arrays.asList("*"));

        // Métodos permitidos
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));

        // Cabeceras permitidas
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin"
        ));

        // Para poder mandar cookies / Authorization
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
