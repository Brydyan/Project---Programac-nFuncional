package ec.edu.upse.backend.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserValidatorTest {

    // USERNAME
    @Test
    void esUsernameValido_conUsernameCorrecto_debeSerTrue() {
        assertTrue(UserValidator.esUsernameValido("Juan_123"));
        assertTrue(UserValidator.esUsernameValido("pepito"));
        assertTrue(UserValidator.esUsernameValido("user.name"));
    }

    @Test
    void esUsernameValido_conEspaciosOSoloEspacios_debeSerFalse() {
        assertFalse(UserValidator.esUsernameValido("   "));
        assertFalse(UserValidator.esUsernameValido(""));
        assertFalse(UserValidator.esUsernameValido(null));
    }

    @Test
    void esUsernameValido_conCaracteresInvalidos_debeSerFalse() {
        assertFalse(UserValidator.esUsernameValido("con espacio"));
        assertFalse(UserValidator.esUsernameValido("con-GuionMedio"));
        assertFalse(UserValidator.esUsernameValido("con@arroba"));
    }

    @Test
    void esUsernameValido_conLongitudFueraDeRango_debeSerFalse() {
        assertFalse(UserValidator.esUsernameValido("ab")); // menos de 3
        assertFalse(UserValidator.esUsernameValido("a".repeat(21))); // más de 20
    }

    @Test
    void normalizarUsername_valido_devuelveMinusculasYTrim() {
        String result = UserValidator.normalizarUsername("  Juanito_99 ");
        assertEquals("juanito_99", result);
    }

    @Test
    void normalizarUsername_invalido_devuelveNull() {
        assertNull(UserValidator.normalizarUsername("   "));
        assertNull(UserValidator.normalizarUsername("no valido!"));
        assertNull(UserValidator.normalizarUsername(null));
    }

    // EMAIL

    @Test
    void esEmailValido_conEmailCorrecto_debeSerTrue() {
        assertTrue(UserValidator.esEmailValido("user@example.com"));
        assertTrue(UserValidator.esEmailValido(" user.name@example.co "));
    }

    @Test
    void esEmailValido_conEmailInvalido_debeSerFalse() {
        assertFalse(UserValidator.esEmailValido("userexample.com"));    // sin @
        assertFalse(UserValidator.esEmailValido("user@"));              // sin dominio
        assertFalse(UserValidator.esEmailValido("@example.com"));       // sin usuario
        assertFalse(UserValidator.esEmailValido("user@ example .com")); // con espacios
        assertFalse(UserValidator.esEmailValido(""));
        assertFalse(UserValidator.esEmailValido(null));
    }

    @Test
    void normalizarEmail_valido_devuelveMinusculasYTrim() {
        String result = UserValidator.normalizarEmail("  UsEr@Example.Com  ");
        assertEquals("user@example.com", result);
    }

    @Test
    void normalizarEmail_invalido_devuelveNull() {
        assertNull(UserValidator.normalizarEmail("userexample.com"));
        assertNull(UserValidator.normalizarEmail("   "));
        assertNull(UserValidator.normalizarEmail(null));
    }
}