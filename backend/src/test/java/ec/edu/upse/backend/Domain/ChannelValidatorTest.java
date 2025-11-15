package ec.edu.upse.backend.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChannelValidatorTest {
    /**
     * NOMBRE Y DESCRIPCIÓN DE CANAL
     * 
     */
    // NOMBRE
    @Test
    void esNombreValido_conNombreCorrecto_debeSerTrue() {
        assertTrue(ChannelValidator.esNombreValido("general"));
        assertTrue(ChannelValidator.esNombreValido("Chat General"));
        assertTrue(ChannelValidator.esNombreValido("canal_123"));
        assertTrue(ChannelValidator.esNombreValido("grupo-#1"));
    }

    @Test
    void esNombreValido_conNullOVacioOSoloEspacios_debeSerFalse() {
        assertFalse(ChannelValidator.esNombreValido(null));
        assertFalse(ChannelValidator.esNombreValido(""));
        assertFalse(ChannelValidator.esNombreValido("   "));
    }

    @Test
    void esNombreValido_conCaracteresInvalidos_debeSerFalse() {
        assertFalse(ChannelValidator.esNombreValido("canal!"));
        assertFalse(ChannelValidator.esNombreValido("canal@"));
        assertFalse(ChannelValidator.esNombreValido("canal%"));
    }

    @Test
    void esNombreValido_conLongitudFueraDeRango_debeSerFalse() {
        assertFalse(ChannelValidator.esNombreValido("ab")); // < 3
        assertFalse(ChannelValidator.esNombreValido("a".repeat(31))); // > 30
    }

    @Test
    void normalizarNombre_valido_devuelveSlugEnMinusculas() {
        String result = ChannelValidator.normalizarNombre("  Chat General  ");
        assertEquals("chat-general", result);

        String result2 = ChannelValidator.normalizarNombre("Canal_#1");
        assertEquals("canal_#1", result2);
    }

    @Test
    void normalizarNombre_invalido_devuelveNull() {
        assertNull(ChannelValidator.normalizarNombre("   "));
        assertNull(ChannelValidator.normalizarNombre("canal!raro"));
        assertNull(ChannelValidator.normalizarNombre("ab"));
    }

    // DESCRIPCIÓN

    @Test
    void esDescripcionValida_nullOVacia_debeSerTrue() {
        assertTrue(ChannelValidator.esDescripcionValida(null));
        assertTrue(ChannelValidator.esDescripcionValida(""));
    }

    @Test
    void esDescripcionValida_conTextoMenorIgualA200_debeSerTrue() {
        String text = "a".repeat(200);
        assertTrue(ChannelValidator.esDescripcionValida(text));
    }

    @Test
    void esDescripcionValida_conTextoMayorA200_debeSerFalse() {
        String text = "a".repeat(201);
        assertFalse(ChannelValidator.esDescripcionValida(text));
    }

    @Test
    void normalizarDescripcion_conNull_devuelveNull() {
        assertNull(ChannelValidator.normalizarDescripcion(null));
    }

    @Test
    void normalizarDescripcion_conSoloEspacios_devuelveNull() {
        assertNull(ChannelValidator.normalizarDescripcion("   "));
    }

    @Test
    void normalizarDescripcion_conTexto_devuelveTrim() {
        String result = ChannelValidator.normalizarDescripcion("  hola descripcion  ");
        assertEquals("hola descripcion", result);
    }
}