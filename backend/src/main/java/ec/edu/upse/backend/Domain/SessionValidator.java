package ec.edu.upse.backend.Domain;

import java.util.Set;

public class SessionValidator {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "active",
            "inactive"
    );

    public static boolean esUserIdValido(String userId) {
        if (userId == null) return false;
        return !userId.trim().isEmpty();
    }

    public static boolean esTokenValido(String token) {
        if (token == null) return false;
        return !token.trim().isEmpty();
    }

    public static boolean esStatusValido(String status) {
        if (status == null) return false;
        String normalized = status.trim().toLowerCase();
        return ESTADOS_VALIDOS.contains(normalized);
    }

    public static String normalizarStatus(String status) {
        if (!esStatusValido(status)) {
            return null;
        }
        return status.trim().toLowerCase();
    }
}