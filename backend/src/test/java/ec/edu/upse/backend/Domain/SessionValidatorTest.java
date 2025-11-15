package ec.edu.upse.backend.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SessionValidatorTest {

    @Test
    void esUserIdValido_conTextoNoVacio_debeSerTrue() {
        assertTrue(SessionValidator.esUserIdValido("user1"));
        assertTrue(SessionValidator.esUserIdValido("  user2  "));
    }

    @Test
    void esUserIdValido_conNullOSoloEspacios_debeSerFalse() {
        assertFalse(SessionValidator.esUserIdValido(null));
        assertFalse(SessionValidator.esUserIdValido(""));
        assertFalse(SessionValidator.esUserIdValido("   "));
    }

    @Test
    void esTokenValido_conTextoNoVacio_debeSerTrue() {
        assertTrue(SessionValidator.esTokenValido("token123"));
        assertTrue(SessionValidator.esTokenValido("  tok  "));
    }

    @Test
    void esTokenValido_conNullOSoloEspacios_debeSerFalse() {
        assertFalse(SessionValidator.esTokenValido(null));
        assertFalse(SessionValidator.esTokenValido(""));
        assertFalse(SessionValidator.esTokenValido("   "));
    }

    @Test
    void esStatusValido_conValoresCorrectos_debeSerTrue() {
        assertTrue(SessionValidator.esStatusValido("active"));
        assertTrue(SessionValidator.esStatusValido("INACTIVE"));
        assertTrue(SessionValidator.esStatusValido("  Active  "));
    }

    @Test
    void esStatusValido_conValoresInvalidos_debeSerFalse() {
        assertFalse(SessionValidator.esStatusValido(null));
        assertFalse(SessionValidator.esStatusValido(""));
        assertFalse(SessionValidator.esStatusValido("activa"));
        assertFalse(SessionValidator.esStatusValido("otro"));
    }

    @Test
    void normalizarStatus_valido_devuelveMinusculasSinEspacios() {
        assertEquals("active", SessionValidator.normalizarStatus(" ACTIVE "));
        assertEquals("inactive", SessionValidator.normalizarStatus("Inactive"));
    }

    @Test
    void normalizarStatus_invalido_devuelveNull() {
        assertNull(SessionValidator.normalizarStatus("otro"));
        assertNull(SessionValidator.normalizarStatus("   "));
        assertNull(SessionValidator.normalizarStatus(null));
    }
}