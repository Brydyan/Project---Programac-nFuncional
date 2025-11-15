package ec.edu.upse.backend.Domain;

import java.util.Set;

public class PresenceValidator {
    /**
     * validar la presencia en tiempo real de un usuario
     * 
     */

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "ONLINE",
            "OFFLINE",
            "AWAY");

    public static boolean esUserIdValido(String userId) {
        if (userId == null)
            return false;
        return !userId.trim().isEmpty();
    }

    public static boolean esStatusValido(String status) {
        if (status == null)
            return false;
        String normalized = status.trim().toUpperCase();
        return ESTADOS_VALIDOS.contains(normalized);
    }

    public static String normalizarStatus(String status) {
        if (!esStatusValido(status)) {
            return null;
        }
        return status.trim().toUpperCase();
    }
}