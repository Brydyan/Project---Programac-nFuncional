package ec.edu.upse.backend.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ContactValidatorTest {

    @Test
    void sonIdsValidos_conIdsDistintosYNoVacios_debeSerTrue() {
        assertTrue(ContactValidator.sonIdsValidos("user1", "user2"));
    }

    @Test
    void sonIdsValidos_conNullOVaciosOIguales_debeSerFalse() {
        assertFalse(ContactValidator.sonIdsValidos(null, "user2"));
        assertFalse(ContactValidator.sonIdsValidos("user1", null));
        assertFalse(ContactValidator.sonIdsValidos("", "user2"));
        assertFalse(ContactValidator.sonIdsValidos("user1", ""));
        assertFalse(ContactValidator.sonIdsValidos("user1", "user1")); // mismo id
    }

    @Test
    void esEstadoValido_conEstadosCorrectos_debeSerTrue() {
        assertTrue(ContactValidator.esEstadoValido("pending"));
        assertTrue(ContactValidator.esEstadoValido("accepted"));
        assertTrue(ContactValidator.esEstadoValido("blocked"));
        assertTrue(ContactValidator.esEstadoValido("  Pending  ")); // espacios y mayúsculas
    }

    @Test
    void esEstadoValido_conEstadoInvalido_debeSerFalse() {
        assertFalse(ContactValidator.esEstadoValido("pendiente"));
        assertFalse(ContactValidator.esEstadoValido("otro"));
        assertFalse(ContactValidator.esEstadoValido(" "));
        assertFalse(ContactValidator.esEstadoValido(null));
    }

    @Test
    void normalizarEstado_valido_devuelveMinusculasSinEspacios() {
        String result = ContactValidator.normalizarEstado("  Pending ");
        assertEquals("pending", result);
    }

    @Test
    void normalizarEstado_invalido_devuelveNull() {
        assertNull(ContactValidator.normalizarEstado("otro"));
        assertNull(ContactValidator.normalizarEstado("   "));
        assertNull(ContactValidator.normalizarEstado(null));
    }
}