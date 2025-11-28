package ec.edu.upse.backend.Domain;

import java.util.regex.Pattern;

public class UserValidator {
   
    /** 
     * Valida si el email tiene un formato correcto.
     * @param email El email a validar.
     * @return true si el email es válido, false en caso contrario.
     * 
     * 
     */
    // Regex sencillita para email (no perfecta, pero útil)
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
 /**
     * Username válido si:
     * - no es null
     * - no está vacío
     * - longitud entre 3 y 20
     * - solo letras, números, guión bajo o punto
     */
    public static boolean esUsernameValido(String username) {
        if (username == null) return false;

        String trimmed = username.trim();
        if (trimmed.isEmpty()) return false;

        int len = trimmed.length();
        if (len < 3 || len > 20) return false;

        // Solo letras, números, _ y .
        return trimmed.matches("^[A-Za-z0-9_.]+$");
    }

    /**
     * Normaliza username:
     * - trim
     * - a minúsculas
     * - si no es válido → devuelve null
     */
    public static String normalizarUsername(String username) {
        if (!esUsernameValido(username)) {
            return null;
        }
        return username.trim().toLowerCase();
    }

    /**
     * Email válido si:
     * - no es null/ vacío
     * - cumple regex sencilla
     */
    public static boolean esEmailValido(String email) {
        if (email == null) return false;

        String trimmed = email.trim();
        if (trimmed.isEmpty()) return false;

        return EMAIL_REGEX.matcher(trimmed).matches();
    }

    /**
     * Normaliza email:
     * - trim
     * - a minúsculas
     * - si no es válido → null
     */
    public static String normalizarEmail(String email) {
        if (!esEmailValido(email)) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    public static ValidationResult<String> validarYNormalizarUsername(String username) {
    if (!esUsernameValido(username)) {
        return ValidationResult.error(
            "Nombre de usuario inválido: 3-20 caracteres."
        );
    }

    return ValidationResult.ok(username.trim().toLowerCase());
}

}