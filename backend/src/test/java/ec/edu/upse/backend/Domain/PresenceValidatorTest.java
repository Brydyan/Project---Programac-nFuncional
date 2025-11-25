package ec.edu.upse.backend.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PresenceValidatorTest {

    @Test
    void esUserIdValido_conTextoNoVacio_debeSerTrue() {
        assertTrue(PresenceValidator.esUserIdValido("user1"));
        assertTrue(PresenceValidator.esUserIdValido("  user2  "));
    }

    @Test
    void esUserIdValido_conNullOSoloEspacios_debeSerFalse() {
        assertFalse(PresenceValidator.esUserIdValido(null));
        assertFalse(PresenceValidator.esUserIdValido(""));
        assertFalse(PresenceValidator.esUserIdValido("   "));
    }

    @Test
    void esStatusValido_conEstadosCorrectos_debeSerTrue() {
        assertTrue(PresenceValidator.esStatusValido("ONLINE"));
        assertTrue(PresenceValidator.esStatusValido("offline"));
        assertTrue(PresenceValidator.esStatusValido(" Away "));
    }

    @Test
    void esStatusValido_conEstadosInvalidos_debeSerFalse() {
        assertFalse(PresenceValidator.esStatusValido(null));
        assertFalse(PresenceValidator.esStatusValido(""));
        assertFalse(PresenceValidator.esStatusValido("busy"));
        assertFalse(PresenceValidator.esStatusValido("desconectado"));
    }

    @Test
    void normalizarStatus_valido_devuelveMayusculasSinEspacios() {
        assertEquals("ONLINE", PresenceValidator.normalizarStatus(" online "));
        assertEquals("OFFLINE", PresenceValidator.normalizarStatus("offline"));
        assertEquals("AWAY", PresenceValidator.normalizarStatus("AwAy"));
    }

    @Test
    void normalizarStatus_invalido_devuelveNull() {
        assertNull(PresenceValidator.normalizarStatus("busy"));
        assertNull(PresenceValidator.normalizarStatus("   "));
        assertNull(PresenceValidator.normalizarStatus(null));
    }
}