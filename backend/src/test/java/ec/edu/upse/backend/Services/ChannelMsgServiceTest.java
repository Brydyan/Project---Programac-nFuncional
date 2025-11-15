package ec.edu.upse.backend.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import ec.edu.upse.backend.Entity.ChannelMsgEntity;
import ec.edu.upse.backend.Repository.ChannelMsgRepository;
import ec.edu.upse.backend.Service.ChannelMsgService;

@ExtendWith(MockitoExtension.class)
class ChannelMsgServiceTest {

    @Mock
    private ChannelMsgRepository channelMsgRepository;

    @InjectMocks
    private ChannelMsgService channelMsgService;

    // CREATE
    @Test
    void save_debeGuardarYRetornarMensajeDeCanal() {
        ChannelMsgEntity msg = new ChannelMsgEntity();
        msg.setId("1");
        msg.setMessageContent("Hola canal");

        when(channelMsgRepository.save(msg)).thenReturn(msg);

        ChannelMsgEntity result = channelMsgService.save(msg);

        assertNotNull(result);
        assertEquals("Hola canal", result.getMessageContent());
        verify(channelMsgRepository).save(msg);
    }

    // READ - getAllMessages
    @Test
    void getAllMessages_debeRetornarListaDeMensajes() {
        ChannelMsgEntity m1 = new ChannelMsgEntity();
        m1.setId("1");
        m1.setMessageContent("Hola");

        ChannelMsgEntity m2 = new ChannelMsgEntity();
        m2.setId("2");
        m2.setMessageContent("Qué tal");

        List<ChannelMsgEntity> list = Arrays.asList(m1, m2);

        when(channelMsgRepository.findAll()).thenReturn(list);

        List<ChannelMsgEntity> result = channelMsgService.getAllMessages();

        assertEquals(2, result.size());
        assertEquals("Hola", result.get(0).getMessageContent());
        assertEquals("Qué tal", result.get(1).getMessageContent());
        verify(channelMsgRepository).findAll();
    }

    // READ - getMessageById
    @Test
    void getMessageById_cuandoExiste_debeRetornarOptionalConMensaje() {
        String id = "1";
        ChannelMsgEntity msg = new ChannelMsgEntity();
        msg.setId(id);
        msg.setMessageContent("Hola");

        when(channelMsgRepository.findById(id)).thenReturn(Optional.of(msg));

        Optional<ChannelMsgEntity> result = channelMsgService.getMessageById(id);

        assertTrue(result.isPresent());
        assertEquals("Hola", result.get().getMessageContent());
        verify(channelMsgRepository).findById(id);
    }

    @Test
    void getMessageById_cuandoNoExiste_debeRetornarOptionalVacio() {
        String id = "99";
        when(channelMsgRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ChannelMsgEntity> result = channelMsgService.getMessageById(id);

        assertFalse(result.isPresent());
        verify(channelMsgRepository).findById(id);
    }

    // READ - getMessageByMessageId
    @Test
    void getMessageByMessageId_debeRetornarOptionalConMensaje() {
        String messageId = "msg-1";
        ChannelMsgEntity msg = new ChannelMsgEntity();
        msg.setId("1");
        msg.setMessageId(messageId);

        when(channelMsgRepository.findByMessageId(messageId)).thenReturn(Optional.of(msg));

        Optional<ChannelMsgEntity> result = channelMsgService.getMessageByMessageId(messageId);

        assertTrue(result.isPresent());
        assertEquals(messageId, result.get().getMessageId());
        verify(channelMsgRepository).findByMessageId(messageId);
    }

    // READ - getMessagesByChannel
    @Test
    void getMessagesByChannel_debeRetornarMensajesDelCanal() {
        String channelId = "channel1";

        ChannelMsgEntity m1 = new ChannelMsgEntity();
        m1.setId("1");
        m1.setChannel(channelId);

        ChannelMsgEntity m2 = new ChannelMsgEntity();
        m2.setId("2");
        m2.setChannel(channelId);

        List<ChannelMsgEntity> list = Arrays.asList(m1, m2);
        when(channelMsgRepository.findByChannel(channelId)).thenReturn(list);

        List<ChannelMsgEntity> result = channelMsgService.getMessagesByChannel(channelId);

        assertEquals(2, result.size());
        assertEquals(channelId, result.get(0).getChannel());
        assertEquals(channelId, result.get(1).getChannel());
        verify(channelMsgRepository).findByChannel(channelId);
    }

    // READ - getMessagesBySender
    @Test
    void getMessagesBySender_debeRetornarMensajesDelRemitente() {
        String senderId = "user1";

        ChannelMsgEntity m1 = new ChannelMsgEntity();
        m1.setId("1");
        m1.setSenderId(senderId);

        List<ChannelMsgEntity> list = Arrays.asList(m1);
        when(channelMsgRepository.findBySenderId(senderId)).thenReturn(list);

        List<ChannelMsgEntity> result = channelMsgService.getMessagesBySender(senderId);

        assertEquals(1, result.size());
        assertEquals(senderId, result.get(0).getSenderId());
        verify(channelMsgRepository).findBySenderId(senderId);
    }

    // READ - getMessagesByStatus
    @Test
    void getMessagesByStatus_debeRetornarMensajesConEseStatus() {
        String status = "SENT";

        ChannelMsgEntity m1 = new ChannelMsgEntity();
        m1.setId("1");
        m1.setStatus(status);

        List<ChannelMsgEntity> list = Arrays.asList(m1);
        when(channelMsgRepository.findByStatus(status)).thenReturn(list);

        List<ChannelMsgEntity> result = channelMsgService.getMessagesByStatus(status);

        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());
        verify(channelMsgRepository).findByStatus(status);
    }

    // UPDATE
    @Test
    void updateMessage_cuandoExiste_debeActualizarContenidoYStatus() {
        String id = "1";

        ChannelMsgEntity existing = new ChannelMsgEntity();
        existing.setId(id);
        existing.setMessageContent("Viejo");
        existing.setStatus("SENT");

        ChannelMsgEntity newData = new ChannelMsgEntity();
        newData.setMessageContent("Nuevo contenido");
        newData.setStatus("READ");

        when(channelMsgRepository.findById(id)).thenReturn(Optional.of(existing));
        when(channelMsgRepository.save(any(ChannelMsgEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChannelMsgEntity result = channelMsgService.updateMessage(id, newData);

        assertNotNull(result);
        assertEquals("Nuevo contenido", result.getMessageContent());
        assertEquals("READ", result.getStatus());

        verify(channelMsgRepository).findById(id);
        verify(channelMsgRepository).save(existing);
    }

    @Test
    void updateMessage_cuandoNoExiste_debeRetornarNull() {
        String id = "99";
        ChannelMsgEntity newData = new ChannelMsgEntity();

        when(channelMsgRepository.findById(id)).thenReturn(Optional.empty());

        ChannelMsgEntity result = channelMsgService.updateMessage(id, newData);

        assertNull(result);
        verify(channelMsgRepository).findById(id);
        verify(channelMsgRepository, never()).save(any());
    }

    // DELETE
    @Test
    void deleteMessage_cuandoExiste_debeBorrarYRetornarTrue() {
        String id = "1";
        when(channelMsgRepository.existsById(id)).thenReturn(true);

        boolean result = channelMsgService.deleteMessage(id);

        assertTrue(result);
        verify(channelMsgRepository).existsById(id);
        verify(channelMsgRepository).deleteById(id);
    }

    @Test
    void deleteMessage_cuandoNoExiste_noBorraYRetornaFalse() {
        String id = "99";
        when(channelMsgRepository.existsById(id)).thenReturn(false);

        boolean result = channelMsgService.deleteMessage(id);

        assertFalse(result);
        verify(channelMsgRepository).existsById(id);
        verify(channelMsgRepository, never()).deleteById(anyString());
    }
}