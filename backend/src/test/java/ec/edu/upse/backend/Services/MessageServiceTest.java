package ec.edu.upse.backend.Services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import ec.edu.upse.backend.Entity.MessageEntity;
import ec.edu.upse.backend.Repository.MessageRepository;
import ec.edu.upse.backend.Service.MessageService;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private MessageService messageService;

    // CREATE
    @Test
    void save_debeGuardarYRetornarMensaje() {
        MessageEntity msg = new MessageEntity();
        msg.setId("1");
        msg.setContent("Hola");

        when(messageRepository.save(msg)).thenReturn(msg);

        MessageEntity result = messageService.save(msg);

        assertNotNull(result);
        assertEquals("Hola", result.getContent());
        verify(messageRepository).save(msg);
    }

    // READ - getAllMessages
    @Test
    void getAllMessages_debeRetornarListaDeMensajes() {
        MessageEntity m1 = new MessageEntity();
        m1.setId("1");
        m1.setContent("Hola");

        MessageEntity m2 = new MessageEntity();
        m2.setId("2");
        m2.setContent("Qué tal");

        List<MessageEntity> list = Arrays.asList(m1, m2);
        when(messageRepository.findAll()).thenReturn(list);

        List<MessageEntity> result = messageService.getAllMessages();

        assertEquals(2, result.size());
        assertEquals("Hola", result.get(0).getContent());
        assertEquals("Qué tal", result.get(1).getContent());
        verify(messageRepository).findAll();
    }

    // READ - getMessageById
    @Test
    void getMessageById_cuandoExiste_debeRetornarOptionalConMensaje() {
        String id = "1";
        MessageEntity msg = new MessageEntity();
        msg.setId(id);
        msg.setContent("Hola");

        when(messageRepository.findById(id)).thenReturn(Optional.of(msg));

        Optional<MessageEntity> result = messageService.getMessageById(id);

        assertTrue(result.isPresent());
        assertEquals("Hola", result.get().getContent());
        verify(messageRepository).findById(id);
    }

    @Test
    void getMessageById_cuandoNoExiste_debeRetornarOptionalVacio() {
        String id = "99";
        when(messageRepository.findById(id)).thenReturn(Optional.empty());

        Optional<MessageEntity> result = messageService.getMessageById(id);

        assertFalse(result.isPresent());
        verify(messageRepository).findById(id);
    }

    // READ - getMessagesBySender
    @Test
    void getMessagesBySender_debeRetornarMensajesDelRemitente() {
        String senderId = "user1";

        MessageEntity m1 = new MessageEntity();
        m1.setId("1");
        m1.setSenderId(senderId);
        m1.setContent("Hola");

        MessageEntity m2 = new MessageEntity();
        m2.setId("2");
        m2.setSenderId(senderId);
        m2.setContent("Cómo estás");

        List<MessageEntity> list = Arrays.asList(m1, m2);
        when(messageRepository.findBySenderId(senderId)).thenReturn(list);

        List<MessageEntity> result = messageService.getMessagesBySender(senderId);

        assertEquals(2, result.size());
        assertEquals(senderId, result.get(0).getSenderId());
        assertEquals(senderId, result.get(1).getSenderId());
        verify(messageRepository).findBySenderId(senderId);
    }

    // READ - getMessagesByReceiver
    @Test
    void getMessagesByReceiver_debeRetornarMensajesDelReceptor() {
        String receiverId = "user2";

        MessageEntity m1 = new MessageEntity();
        m1.setId("1");
        m1.setReceiverId(receiverId);

        List<MessageEntity> list = Arrays.asList(m1);
        when(messageRepository.findByReceiverId(receiverId)).thenReturn(list);

        List<MessageEntity> result = messageService.getMessagesByReceiver(receiverId);

        assertEquals(1, result.size());
        assertEquals(receiverId, result.get(0).getReceiverId());
        verify(messageRepository).findByReceiverId(receiverId);
    }

    // READ - getMessagesByChannel
    @Test
    void getMessagesByChannel_debeRetornarMensajesDelCanal() {
        String channelId = "channel1";

        MessageEntity m1 = new MessageEntity();
        m1.setId("1");
        m1.setChannelId(channelId);

        MessageEntity m2 = new MessageEntity();
        m2.setId("2");
        m2.setChannelId(channelId);

        List<MessageEntity> list = Arrays.asList(m1, m2);
        when(messageRepository.findByChannelId(channelId)).thenReturn(list);

        List<MessageEntity> result = messageService.getMessagesByChannel(channelId);

        assertEquals(2, result.size());
        assertEquals(channelId, result.get(0).getChannelId());
        assertEquals(channelId, result.get(1).getChannelId());
        verify(messageRepository).findByChannelId(channelId);
    }

    // UPDATE
    @Test
    void updateMessage_cuandoExiste_debeActualizarContenidoYMarcarEditado() {
        String id = "1";

        MessageEntity existing = new MessageEntity();
        existing.setId(id);
        existing.setContent("Viejo");
        existing.setEdited(false);

        MessageEntity newData = new MessageEntity();
        newData.setContent("Nuevo contenido");

        when(messageRepository.findById(id)).thenReturn(Optional.of(existing));
        when(messageRepository.save(any(MessageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageEntity result = messageService.updateMessage(id, newData);

        assertNotNull(result);
        assertEquals("Nuevo contenido", result.getContent());
        assertTrue(result.isEdited());

        verify(messageRepository).findById(id);
        verify(messageRepository).save(existing);
    }

    @Test
    void updateMessage_cuandoNoExiste_debeRetornarNull() {
        String id = "99";
        MessageEntity newData = new MessageEntity();
        newData.setContent("Algo");

        when(messageRepository.findById(id)).thenReturn(Optional.empty());

        MessageEntity result = messageService.updateMessage(id, newData);

        assertNull(result);
        verify(messageRepository).findById(id);
        verify(messageRepository, never()).save(any());
    }

    // DELETE
    @Test
    void deleteMessage_cuandoExiste_debeBorrarYRetornarTrue() {
        String id = "1";
        when(messageRepository.existsById(id)).thenReturn(true);

        boolean result = messageService.deleteMessage(id);

        assertTrue(result);
        verify(messageRepository).existsById(id);
        verify(messageRepository).deleteById(id);
    }

    @Test
    void deleteMessage_cuandoNoExiste_noBorraYRetornaFalse() {
        String id = "99";
        when(messageRepository.existsById(id)).thenReturn(false);

        boolean result = messageService.deleteMessage(id);

        assertFalse(result);
        verify(messageRepository).existsById(id);
        verify(messageRepository, never()).deleteById(anyString());
    }
    
    @Test
    void save_conContenidoInvalido_debeLanzarExcepcion() {
        MessageEntity msg = new MessageEntity();
        msg.setId("1");
        msg.setContent("   "); // solo espacios

        assertThrows(IllegalArgumentException.class, () -> {
            messageService.save(msg);
        });

        verify(messageRepository, never()).save(any());
    }

    @Test
    void updateMessage_conContenidoInvalido_debeLanzarExcepcion() {
        String id = "1";
        MessageEntity existing = new MessageEntity();
        existing.setId(id);
        existing.setContent("Viejo");

        MessageEntity newData = new MessageEntity();
        newData.setContent("   "); // contenido inválido

        when(messageRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> {
            messageService.updateMessage(id, newData);
        });

        verify(messageRepository, never()).save(any());
    }


}