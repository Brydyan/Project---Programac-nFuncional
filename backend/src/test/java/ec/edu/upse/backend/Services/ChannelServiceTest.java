package ec.edu.upse.backend.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ec.edu.upse.backend.Entity.ChannelEntity;
import ec.edu.upse.backend.Repository.ChannelRepository;
import ec.edu.upse.backend.Service.ChannelService;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {
    /**
     * CRUD de Canales
     * 
     */

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private ChannelService channelService;

    // CREATE
    @Test
    void save_conNombreValido_debeGuardarYRetornarCanal() {
        ChannelEntity channel = new ChannelEntity();
        channel.setId("1");
        channel.setName("  Chat General "); // se normaliza a "chat-general"

        when(channelRepository.save(any(ChannelEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChannelEntity result = channelService.save(channel);

        assertNotNull(result);
        assertEquals("chat-general", result.getName());
        verify(channelRepository).save(any(ChannelEntity.class));
    }

    @Test
    void save_conNombreInvalido_debeLanzarExcepcion() {
        ChannelEntity channel = new ChannelEntity();
        channel.setId("1");
        channel.setName("  !!  "); // inválido para nuestro ChannelValidator

        assertThrows(IllegalArgumentException.class, () -> {
            channelService.save(channel);
        });

        verify(channelRepository, never()).save(any());
    }

    // READ - getAllChannels
    @Test
    void getAllChannels_debeRetornarListaDeCanales() {
        ChannelEntity c1 = new ChannelEntity();
        c1.setId("1");
        c1.setName("general");

        ChannelEntity c2 = new ChannelEntity();
        c2.setId("2");
        c2.setName("random");

        List<ChannelEntity> list = Arrays.asList(c1, c2);
        when(channelRepository.findAll()).thenReturn(list);

        List<ChannelEntity> result = channelService.getAllChannels();

        assertEquals(2, result.size());
        assertEquals("general", result.get(0).getName());
        assertEquals("random", result.get(1).getName());
        verify(channelRepository).findAll();
    }

    // READ - getChannelById
    @Test
    void getChannelById_cuandoExiste_debeRetornarOptionalConCanal() {
        String id = "1";
        ChannelEntity channel = new ChannelEntity();
        channel.setId(id);
        channel.setName("general");

        when(channelRepository.findById(id)).thenReturn(Optional.of(channel));

        Optional<ChannelEntity> result = channelService.getChannelById(id);

        assertTrue(result.isPresent());
        assertEquals("general", result.get().getName());
        verify(channelRepository).findById(id);
    }

    @Test
    void getChannelById_cuandoNoExiste_debeRetornarOptionalVacio() {
        String id = "99";
        when(channelRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ChannelEntity> result = channelService.getChannelById(id);

        assertFalse(result.isPresent());
        verify(channelRepository).findById(id);
    }

    // UPDATE
    @Test
    void updateChannel_cuandoExisteYNombreValido_debeActualizarYGuardar() {
        String id = "1";

        ChannelEntity existing = new ChannelEntity();
        existing.setId(id);
        existing.setName("general");
        existing.setType("TEXT");

        ChannelEntity newData = new ChannelEntity();
        newData.setName("  Nuevo Canal ");
        newData.setType("VOICE");

        when(channelRepository.findById(id)).thenReturn(Optional.of(existing));
        when(channelRepository.save(any(ChannelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChannelEntity result = channelService.updateChannel(id, newData);

        assertNotNull(result);
        assertEquals("nuevo-canal", result.getName()); // normalizado
        assertEquals("VOICE", result.getType());

        verify(channelRepository).findById(id);
        verify(channelRepository).save(existing);
    }

    @Test
    void updateChannel_cuandoExisteYNombreInvalido_debeLanzarExcepcion() {
        String id = "1";

        ChannelEntity existing = new ChannelEntity();
        existing.setId(id);
        existing.setName("general");

        ChannelEntity newData = new ChannelEntity();
        newData.setName("   !!   "); // nombre inválido

        when(channelRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> {
            channelService.updateChannel(id, newData);
        });

        verify(channelRepository).findById(id);
        verify(channelRepository, never()).save(any());
    }

    @Test
    void updateChannel_cuandoNoExiste_debeRetornarNull() {
        String id = "99";
        ChannelEntity newData = new ChannelEntity();

        when(channelRepository.findById(id)).thenReturn(Optional.empty());

        ChannelEntity result = channelService.updateChannel(id, newData);

        assertNull(result);
        verify(channelRepository).findById(id);
        verify(channelRepository, never()).save(any());
    }

    // DELETE
    @Test
    void deleteChannel_cuandoExiste_debeBorrarYRetornarTrue() {
        String id = "1";
        when(channelRepository.existsById(id)).thenReturn(true);

        boolean result = channelService.deleteChannel(id);

        assertTrue(result);
        verify(channelRepository).existsById(id);
        verify(channelRepository).deleteById(id);
    }

    @Test
    void deleteChannel_cuandoNoExiste_noBorraYRetornaFalse() {
        String id = "99";
        when(channelRepository.existsById(id)).thenReturn(false);

        boolean result = channelService.deleteChannel(id);

        assertFalse(result);
        verify(channelRepository).existsById(id);
        verify(channelRepository, never()).deleteById(anyString());
    }
}