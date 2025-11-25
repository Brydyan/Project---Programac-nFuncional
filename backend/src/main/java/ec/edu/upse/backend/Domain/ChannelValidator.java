package ec.edu.upse.backend.Domain;

public class ChannelValidator {

    /**
     * Nombre de canal válido si:
     * - no es null
     * - no está vacío ni son sólo espacios
     * - longitud entre 3 y 30
     * - solo letras, números, espacios, guion medio, guion bajo y #
     */
    public static boolean esNombreValido(String name) {
        if (name == null)
            return false;

        String trimmed = name.trim();
        if (trimmed.isEmpty())
            return false;

        int len = trimmed.length();
        if (len < 3 || len > 30)
            return false;

        // Letras, números, espacio, -, _, #
        return trimmed.matches("^[A-Za-z0-9 _#-]+$");
    }

    /**
     * Normaliza el nombre del canal:
     * - si no es válido → devuelve null
     * - si es válido:
     * - trim
     * - a minúsculas
     * - reemplaza espacios por '-'
     */
    public static String normalizarNombre(String name) {
        if (!esNombreValido(name)) {
            return null;
        }
        String trimmed = name.trim().toLowerCase();
        return trimmed.replaceAll("\\s+", "-");
    }

    /**
     * Descripción válida si:
     * - es null → OK
     * - no es null y longitud <= 200
     */
    public static boolean esDescripcionValida(String descripcion) {
        if (descripcion == null)
            return true; // opcional
        return descripcion.length() <= 200;
    }

    /**
     * Normaliza descripción:
     * - si es null → null
     * - si no es null → trim (pero no cambia nada más)
     * - si luego de trim es vacía → devuelve null
     */
    public static String normalizarDescripcion(String descripcion) {
        if (descripcion == null)
            return null;
        String trimmed = descripcion.trim();
        if (trimmed.isEmpty())
            return null;
        return trimmed;
    }
}