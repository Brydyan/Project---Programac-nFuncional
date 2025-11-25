package ec.edu.upse.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.upse.backend.Entity.ChannelMsgEntity;
import ec.edu.upse.backend.Service.ChannelMsgService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ChannelMsgController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad (401/403)
class ChannelMsgControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChannelMsgService channelMsgService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/app/v1/channel-messages";

    // CREATE
    @Test
    void create_debeRetornar200YMensajeCreado() throws Exception {
        ChannelMsgEntity msg = new ChannelMsgEntity();
        msg.setId("1");
        msg.setMessageId("msg-1");
        msg.setMessageContent("Hola canal");

        when(channelMsgService.save(any(ChannelMsgEntity.class))).thenReturn(msg);

        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.messageId").value("msg-1"))
                .andExpect(jsonPath("$.messageContent").value("Hola canal"));

        verify(channelMsgService).save(any(ChannelMsgEntity.class));
    }

    // READ - getAll
    @Test
    void getAll_debeRetornar200YListaDeMensajes() throws Exception {
        ChannelMsgEntity m1 = new ChannelMsgEntity();
        m1.setId("1");
        m1.setMessageContent("Hola");

        ChannelMsgEntity m2 = new ChannelMsgEntity();
        m2.setId("2");
        m2.setMessageContent("Qué tal");

        List<ChannelMsgEntity> list = Arrays.asList(m1, m2);

        when(channelMsgService.getAllMessages()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].messageContent").value("Hola"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].messageContent").value("Qué tal"));

        verify(channelMsgService).getAllMessages();
    }

    // READ - getById
    @Test
    void getById_cuandoExiste_debeRetornar200() throws Exception {
        String id = "1";
        ChannelMsgEntity msg = new ChannelMsgEntity();
        msg.setId(id);
        msg.setMessageContent("Hola canal");

        when(channelMsgService.getMessageById(id)).thenReturn(Optional.of(msg));

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.messageContent").value("Hola canal"));

        verify(channelMsgService).getMessageById(id);
    }

    @Test
    void getById_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";
        when(channelMsgService.getMessageById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());

        verify(channelMsgService).getMessageById(id);
    }

    // READ - getByMessageId
    @Test
    void getByMessageId_cuandoExiste_debeRetornar200() throws Exception {
        String messageId = "msg-1";
        ChannelMsgEntity msg = new ChannelMsgEntity();
        msg.setId("1");
        msg.setMessageId(messageId);

        when(channelMsgService.getMessageByMessageId(messageId)).thenReturn(Optional.of(msg));

        mockMvc.perform(get(BASE_URL + "/message-id/" + messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(messageId));

        verify(channelMsgService).getMessageByMessageId(messageId);
    }

    @Test
    void getByMessageId_cuandoNoExiste_debeRetornar404() throws Exception {
        String messageId = "msg-99";

        when(channelMsgService.getMessageByMessageId(messageId)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/message-id/" + messageId))
                .andExpect(status().isNotFound());

        verify(channelMsgService).getMessageByMessageId(messageId);
    }

    // READ - getByChannel
    @Test
    void getByChannel_debeRetornar200YLista() throws Exception {
        String channelId = "channel-1";

        ChannelMsgEntity m1 = new ChannelMsgEntity();
        m1.setId("1");
        m1.setChannel(channelId);

        ChannelMsgEntity m2 = new ChannelMsgEntity();
        m2.setId("2");
        m2.setChannel(channelId);

        List<ChannelMsgEntity> list = Arrays.asList(m1, m2);

        when(channelMsgService.getMessagesByChannel(channelId)).thenReturn(list);

        mockMvc.perform(get(BASE_URL + "/channel/" + channelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(channelMsgService).getMessagesByChannel(channelId);
    }

    // READ - getBySender
    @Test
    void getBySender_debeRetornar200YLista() throws Exception {
        String senderId = "user1";

        ChannelMsgEntity m1 = new ChannelMsgEntity();
        m1.setId("1");
        m1.setSenderId(senderId);

        List<ChannelMsgEntity> list = List.of(m1);

        when(channelMsgService.getMessagesBySender(senderId)).thenReturn(list);

        mockMvc.perform(get(BASE_URL + "/sender/" + senderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].senderId").value(senderId));

        verify(channelMsgService).getMessagesBySender(senderId);
    }

    // READ - getByStatus
    @Test
    void getByStatus_debeRetornar200YLista() throws Exception {
        String status = "SENT";

        ChannelMsgEntity m1 = new ChannelMsgEntity();
        m1.setId("1");
        m1.setStatus(status);

        List<ChannelMsgEntity> list = List.of(m1);

        when(channelMsgService.getMessagesByStatus(status)).thenReturn(list);

        mockMvc.perform(get(BASE_URL + "/status/" + status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value(status));

        verify(channelMsgService).getMessagesByStatus(status);
    }

    // UPDATE
    @Test
    void update_cuandoExiste_debeRetornar200YMensajeActualizado() throws Exception {
        String id = "1";

        ChannelMsgEntity newData = new ChannelMsgEntity();
        newData.setMessageContent("Nuevo contenido");
        newData.setStatus("READ");

        ChannelMsgEntity updated = new ChannelMsgEntity();
        updated.setId(id);
        updated.setMessageContent("Nuevo contenido");
        updated.setStatus("READ");

        when(channelMsgService.updateMessage(eq(id), any(ChannelMsgEntity.class))).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.messageContent").value("Nuevo contenido"))
                .andExpect(jsonPath("$.status").value("READ"));

        verify(channelMsgService).updateMessage(eq(id), any(ChannelMsgEntity.class));
    }

    @Test
    void update_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";

        ChannelMsgEntity newData = new ChannelMsgEntity();
        newData.setMessageContent("Nuevo contenido");

        when(channelMsgService.updateMessage(eq(id), any(ChannelMsgEntity.class))).thenReturn(null);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newData)))
                .andExpect(status().isNotFound());

        verify(channelMsgService).updateMessage(eq(id), any(ChannelMsgEntity.class));
    }

    // DELETE
    @Test
    void delete_cuandoExiste_debeRetornar204() throws Exception {
        String id = "1";
        when(channelMsgService.deleteMessage(id)).thenReturn(true);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        verify(channelMsgService).deleteMessage(id);
    }

    @Test
    void delete_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";
        when(channelMsgService.deleteMessage(id)).thenReturn(false);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());

        verify(channelMsgService).deleteMessage(id);
    }
}