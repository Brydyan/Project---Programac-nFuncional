package ec.edu.upse.backend.Domain;

/**
 * Representa el resultado de una validación funcional:
 *  - Ok(value)  → éxito
 *  - Error(msg) → falla
 *
 * Es genérico: ValidationResult<String>, ValidationResult<Integer>, etc.
 */
public interface ValidationResult<T> {

    boolean isValid();
    T getValue();
    String getError();

    static <T> ValidationResult<T> ok(T value) {
        return new Ok<>(value);
    }

    static <T> ValidationResult<T> error(String message) {
        return new Error<>(message);
    }

    /**
     * Caso OK: contiene un valor válido.
     */
    final class Ok<T> implements ValidationResult<T> {
        private final T value;

        public Ok(T value) {
            this.value = value;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public String getError() {
            return null;
        }
    }

    /**
     * Caso Error: contiene un mensaje de error.
     */
    final class Error<T> implements ValidationResult<T> {
        private final String message;

        public Error(String message) {
            this.message = message;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public T getValue() {
            return null;
        }

        @Override
        public String getError() {
            return message;
        }
    }
}
