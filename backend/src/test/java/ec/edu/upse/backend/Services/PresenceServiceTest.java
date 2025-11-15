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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ec.edu.upse.backend.Entity.PresenceEntity;
import ec.edu.upse.backend.Repository.PresenceRepository;
import ec.edu.upse.backend.Service.PresenceService;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {
    
    @Mock
    private PresenceRepository presenceRepository;

    @InjectMocks
    private PresenceService presenceService;

    // CREATE
    @Test
    void save_conDatosValidos_debeGuardarYRetornarPresence() {
        PresenceEntity presence = new PresenceEntity();
        presence.setId("1");
        presence.setUserId("user1");
        presence.setStatus("online"); // se normaliza a "ONLINE"
        presence.setLastSeen(Instant.now());

        when(presenceRepository.save(any(PresenceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PresenceEntity result = presenceService.save(presence);

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("ONLINE", result.getStatus());
        verify(presenceRepository).save(any(PresenceEntity.class));
    }

    @Test
    void save_conUserIdInvalido_debeLanzarExcepcion() {
        PresenceEntity presence = new PresenceEntity();
        presence.setId("1");
        presence.setUserId("   "); // inválido
        presence.setStatus("ONLINE");

        assertThrows(IllegalArgumentException.class, () -> {
            presenceService.save(presence);
        });

        verify(presenceRepository, never()).save(any());
    }

    @Test
    void save_conStatusInvalido_debeLanzarExcepcion() {
        PresenceEntity presence = new PresenceEntity();
        presence.setId("1");
        presence.setUserId("user1");
        presence.setStatus("BUSY"); // inválido según nuestro validator

        assertThrows(IllegalArgumentException.class, () -> {
            presenceService.save(presence);
        });

        verify(presenceRepository, never()).save(any());
    }

    // READ - getAll
    @Test
    void getAll_debeRetornarListaDePresences() {
        PresenceEntity p1 = new PresenceEntity();
        p1.setId("1");
        p1.setUserId("user1");
        p1.setStatus("ONLINE");

        PresenceEntity p2 = new PresenceEntity();
        p2.setId("2");
        p2.setUserId("user2");
        p2.setStatus("OFFLINE");

        List<PresenceEntity> list = Arrays.asList(p1, p2);
        when(presenceRepository.findAll()).thenReturn(list);

        List<PresenceEntity> result = presenceService.getAll();

        assertEquals(2, result.size());
        verify(presenceRepository).findAll();
    }

    // READ - getById
    @Test
    void getById_cuandoExiste_debeRetornarOptionalConPresence() {
        String id = "1";
        PresenceEntity presence = new PresenceEntity();
        presence.setId(id);

        when(presenceRepository.findById(id)).thenReturn(Optional.of(presence));

        Optional<PresenceEntity> result = presenceService.getById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(presenceRepository).findById(id);
    }

    @Test
    void getById_cuandoNoExiste_debeRetornarOptionalVacio() {
        String id = "99";
        when(presenceRepository.findById(id)).thenReturn(Optional.empty());

        Optional<PresenceEntity> result = presenceService.getById(id);

        assertFalse(result.isPresent());
        verify(presenceRepository).findById(id);
    }

    // READ - getByUser
    @Test
    void getByUser_debeRetornarPresencesDelUsuario() {
        String userId = "user1";

        PresenceEntity p1 = new PresenceEntity();
        p1.setId("1");
        p1.setUserId(userId);

        List<PresenceEntity> list = List.of(p1);
        when(presenceRepository.findByUserId(userId)).thenReturn(list);

        List<PresenceEntity> result = presenceService.getByUser(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(presenceRepository).findByUserId(userId);
    }

    // UPDATE - updateStatus
    @Test
    void updateStatus_cuandoExisteYStatusValido_debeActualizar() {
        String id = "1";

        PresenceEntity existing = new PresenceEntity();
        existing.setId(id);
        existing.setUserId("user1");
        existing.setStatus("OFFLINE");
        existing.setLastSeen(Instant.now().minusSeconds(60));

        when(presenceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(presenceRepository.save(any(PresenceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PresenceEntity result = presenceService.updateStatus(id, "away");

        assertNotNull(result);
        assertEquals("AWAY", result.getStatus());
        verify(presenceRepository).findById(id);
        verify(presenceRepository).save(existing);
    }

    @Test
    void updateStatus_cuandoExisteYStatusInvalido_debeLanzarExcepcion() {
        String id = "1";

        PresenceEntity existing = new PresenceEntity();
        existing.setId(id);
        existing.setUserId("user1");
        existing.setStatus("OFFLINE");

        when(presenceRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> {
            presenceService.updateStatus(id, "BUSY");
        });

        verify(presenceRepository).findById(id);
        verify(presenceRepository, never()).save(any());
    }

    @Test
    void updateStatus_cuandoNoExiste_debeRetornarNull() {
        String id = "99";
        when(presenceRepository.findById(id)).thenReturn(Optional.empty());

        PresenceEntity result = presenceService.updateStatus(id, "ONLINE");

        assertNull(result);
        verify(presenceRepository).findById(id);
        verify(presenceRepository, never()).save(any());
    }

    // DELETE
    @Test
    void delete_cuandoExiste_debeBorrarYRetornarTrue() {
        String id = "1";
        when(presenceRepository.existsById(id)).thenReturn(true);

        boolean result = presenceService.delete(id);

        assertTrue(result);
        verify(presenceRepository).existsById(id);
        verify(presenceRepository).deleteById(id);
    }

    @Test
    void delete_cuandoNoExiste_noBorraYRetornaFalse() {
        String id = "99";
        when(presenceRepository.existsById(id)).thenReturn(false);

        boolean result = presenceService.delete(id);

        assertFalse(result);
        verify(presenceRepository).existsById(id);
        verify(presenceRepository, never()).deleteById(anyString());
    }
}