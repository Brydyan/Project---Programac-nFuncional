package ec.edu.upse.backend.Domain;

public class MessageValidator {

    /**
     * Devuelve true si el contenido del mensaje es válido:
     * - no es null
     * - no está vacío
     * - no es solo espacios en blanco
     */
    public static boolean esContenidoValido(String content) {
        if (content == null) return false;
        return !content.trim().isEmpty();
    }

    /**
     * Normaliza el contenido:
     * - si es null o vacío → devuelve null
     * - si tiene texto → devuelve el texto recortado (trim)
     */
    public static String normalizarContenido(String content) {
        if (!esContenidoValido(content)) {
            return null;
        }
        return content.trim();
    }

    public static ValidationResult<String> validarYNormalizarContenido(String content) {
    if (!esContenidoValido(content)) {
            return ValidationResult.error("El mensaje está vacío.");
        }

        return ValidationResult.ok(content.trim());
    }

    public static ValidationResult<String> prepararMensajeUsuario(String input) {
        return validarYNormalizarContenido(
            UserValidator.validarYNormalizarUsername(input).getValue()
        );
    }

}