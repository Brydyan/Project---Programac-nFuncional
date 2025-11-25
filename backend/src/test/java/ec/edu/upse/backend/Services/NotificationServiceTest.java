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

import ec.edu.upse.backend.Entity.NotificationEntity;
import ec.edu.upse.backend.Repository.NotificationRepository;
import ec.edu.upse.backend.Service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    // CREATE
    @Test
    void save_conDatosValidos_debeGuardarYRetornarNotificacion() {
        NotificationEntity notif = new NotificationEntity();
        notif.setId("1");
        notif.setUserId("user1");
        notif.setMessageId("msg1");
        notif.setRead(false);
        notif.setCreatedAt(Instant.now());

        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationEntity result = notificationService.save(notif);

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("msg1", result.getMessageId());
        assertFalse(result.isRead());
        verify(notificationRepository).save(any(NotificationEntity.class));
    }

    @Test
    void save_conIdsInvalidos_debeLanzarExcepcion() {
        NotificationEntity notif = new NotificationEntity();
        notif.setId("1");
        notif.setUserId("  "); // inválido
        notif.setMessageId("msg1");

        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.save(notif);
        });

        verify(notificationRepository, never()).save(any());
    }

    // READ - getAll
    @Test
    void getAll_debeRetornarListaDeNotificaciones() {
        NotificationEntity n1 = new NotificationEntity();
        n1.setId("1");
        n1.setUserId("user1");
        n1.setMessageId("msg1");

        NotificationEntity n2 = new NotificationEntity();
        n2.setId("2");
        n2.setUserId("user2");
        n2.setMessageId("msg2");

        List<NotificationEntity> list = Arrays.asList(n1, n2);
        when(notificationRepository.findAll()).thenReturn(list);

        List<NotificationEntity> result = notificationService.getAll();

        assertEquals(2, result.size());
        verify(notificationRepository).findAll();
    }

    // READ - getById
    @Test
    void getById_cuandoExiste_debeRetornarOptionalConNotificacion() {
        String id = "1";
        NotificationEntity notif = new NotificationEntity();
        notif.setId(id);

        when(notificationRepository.findById(id)).thenReturn(Optional.of(notif));

        Optional<NotificationEntity> result = notificationService.getById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(notificationRepository).findById(id);
    }

    @Test
    void getById_cuandoNoExiste_debeRetornarOptionalVacio() {
        String id = "99";
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        Optional<NotificationEntity> result = notificationService.getById(id);

        assertFalse(result.isPresent());
        verify(notificationRepository).findById(id);
    }

    // READ - getByUser
    @Test
    void getByUser_debeRetornarNotificacionesDelUsuario() {
        String userId = "user1";

        NotificationEntity n1 = new NotificationEntity();
        n1.setId("1");
        n1.setUserId(userId);

        List<NotificationEntity> list = List.of(n1);
        when(notificationRepository.findByUserId(userId)).thenReturn(list);

        List<NotificationEntity> result = notificationService.getByUser(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(notificationRepository).findByUserId(userId);
    }

    // UPDATE - markAsRead
    @Test
    void markAsRead_cuandoExiste_debeMarcarLeidaYGuardar() {
        String id = "1";

        NotificationEntity existing = new NotificationEntity();
        existing.setId(id);
        existing.setUserId("user1");
        existing.setMessageId("msg1");
        existing.setRead(false);

        when(notificationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationEntity result = notificationService.markAsRead(id);

        assertNotNull(result);
        assertTrue(result.isRead());
        verify(notificationRepository).findById(id);
        verify(notificationRepository).save(existing);
    }

    @Test
    void markAsRead_cuandoNoExiste_debeRetornarNull() {
        String id = "99";
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        NotificationEntity result = notificationService.markAsRead(id);

        assertNull(result);
        verify(notificationRepository).findById(id);
        verify(notificationRepository, never()).save(any());
    }

    // DELETE
    @Test
    void delete_cuandoExiste_debeBorrarYRetornarTrue() {
        String id = "1";
        when(notificationRepository.existsById(id)).thenReturn(true);

        boolean result = notificationService.delete(id);

        assertTrue(result);
        verify(notificationRepository).existsById(id);
        verify(notificationRepository).deleteById(id);
    }

    @Test
    void delete_cuandoNoExiste_noBorraYRetornaFalse() {
        String id = "99";
        when(notificationRepository.existsById(id)).thenReturn(false);

        boolean result = notificationService.delete(id);

        assertFalse(result);
        verify(notificationRepository).existsById(id);
        verify(notificationRepository, never()).deleteById(anyString());
    }
}