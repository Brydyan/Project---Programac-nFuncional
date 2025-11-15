package ec.edu.upse.backend.Domain;

import java.util.Set;

public class ContactValidator {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "pending",
            "accepted",
            "blocked");

    public static boolean sonIdsValidos(String userId, String contactId) {
        if (userId == null || contactId == null)
            return false;
        if (userId.trim().isEmpty() || contactId.trim().isEmpty())
            return false;
        // no permitir agregarse a sí mismo
        return !userId.equals(contactId);
    }

    public static boolean esEstadoValido(String state) {
        if (state == null)
            return false;
        String normalized = state.trim().toLowerCase();
        return ESTADOS_VALIDOS.contains(normalized);
    }

    public static String normalizarEstado(String state) {
        if (!esEstadoValido(state))
            return null;
        return state.trim().toLowerCase();
    }
}