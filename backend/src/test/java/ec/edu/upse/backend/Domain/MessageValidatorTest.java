package ec.edu.upse.backend.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MessageValidatorTest {

    @Test
    void esContenidoValido_conTextoNormal_debeSerTrue() {
        assertTrue(MessageValidator.esContenidoValido("Hola mundo"));
    }

    @Test
    void esContenidoValido_conEspaciosSolo_debeSerFalse() {
        assertFalse(MessageValidator.esContenidoValido("    "));
    }

    @Test
    void esContenidoValido_conNull_debeSerFalse() {
        assertFalse(MessageValidator.esContenidoValido(null));
    }

    @Test
    void esContenidoValido_conStringVacio_debeSerFalse() {
        assertFalse(MessageValidator.esContenidoValido(""));
    }

    @Test
    void normalizarContenido_conTexto_conEspacios_debeRecortar() {
        String result = MessageValidator.normalizarContenido("   hola   ");
        assertEquals("hola", result);
    }

    @Test
    void normalizarContenido_conContenidoInvalido_debeRetornarNull() {
        assertNull(MessageValidator.normalizarContenido("   "));
        assertNull(MessageValidator.normalizarContenido(null));
        assertNull(MessageValidator.normalizarContenido(""));
    }
}