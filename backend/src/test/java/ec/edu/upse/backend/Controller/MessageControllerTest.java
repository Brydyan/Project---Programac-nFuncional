package ec.edu.upse.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.upse.backend.Entity.MessageEntity;
import ec.edu.upse.backend.Service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
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
@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/app/v1/messages";

    // CREATE
    @Test
    void create_debeRetornar200YMensajeCreado() throws Exception {
        MessageEntity msg = new MessageEntity();
        msg.setId("1");
        msg.setSenderId("user1");
        msg.setReceiverId("user2");
        msg.setChannelId("channel1"); // si tu campo se llama distinto, ajústalo
        msg.setContent("Hola");
        msg.setTimestamp(Instant.now());
        msg.setEdited(false);

        when(messageService.save(any(MessageEntity.class))).thenReturn(msg);

        mockMvc.perform(post(BASE_URL)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.senderId").value("user1"))
                .andExpect(jsonPath("$.receiverId").value("user2"))
                .andExpect(jsonPath("$.channelId").value("channel1"))
                .andExpect(jsonPath("$.content").value("Hola"));

        verify(messageService).save(any(MessageEntity.class));
    }

    // READ - getAll
    @Test
    void getAll_debeRetornar200YListaDeMensajes() throws Exception {
        MessageEntity m1 = new MessageEntity();
        m1.setId("1");
        m1.setContent("Hola");

        MessageEntity m2 = new MessageEntity();
        m2.setId("2");
        m2.setContent("Qué tal");

        List<MessageEntity> list = Arrays.asList(m1, m2);

        when(messageService.getAllMessages()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        verify(messageService).getAllMessages();
    }

    // READ - getById
    @Test
    void getById_cuandoExiste_debeRetornar200YMensaje() throws Exception {
        String id = "1";

        MessageEntity msg = new MessageEntity();
        msg.setId(id);
        msg.setContent("Hola");

        when(messageService.getMessageById(id)).thenReturn(Optional.of(msg));

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.content").value("Hola"));

        verify(messageService).getMessageById(id);
    }

    @Test
    void getById_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";
        when(messageService.getMessageById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());

        verify(messageService).getMessageById(id);
    }

    // READ - getBySender
    @Test
    void getBySender_debeRetornar200YLista() throws Exception {
        String senderId = "user1";

        MessageEntity m1 = new MessageEntity();
        m1.setId("1");
        m1.setSenderId(senderId);

        List<MessageEntity> list = List.of(m1);

        when(messageService.getMessagesBySender(senderId)).thenReturn(list);

        mockMvc.perform(get(BASE_URL + "/sender/" + senderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].senderId").value(senderId));

        verify(messageService).getMessagesBySender(senderId);
    }

    // READ - getByReceiver
    @Test
    void getByReceiver_debeRetornar200YLista() throws Exception {
        String receiverId = "user2";

        MessageEntity m1 = new MessageEntity();
        m1.setId("1");
        m1.setReceiverId(receiverId);

        List<MessageEntity> list = List.of(m1);

        when(messageService.getMessagesByReceiver(receiverId)).thenReturn(list);

        mockMvc.perform(get(BASE_URL + "/receiver/" + receiverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].receiverId").value(receiverId));

        verify(messageService).getMessagesByReceiver(receiverId);
    }

    // READ - getByChannel
    @Test
    void getByChannel_debeRetornar200YLista() throws Exception {
        String channelId = "channel1";

        MessageEntity m1 = new MessageEntity();
        m1.setId("1");
        m1.setChannelId(channelId);

        List<MessageEntity> list = List.of(m1);

        when(messageService.getMessagesByChannel(channelId)).thenReturn(list);

        mockMvc.perform(get(BASE_URL + "/channel/" + channelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].channelId").value(channelId));

        verify(messageService).getMessagesByChannel(channelId);
    }

    // UPDATE
    @Test
    void update_cuandoExiste_debeRetornar200YMensajeActualizado() throws Exception {
        String id = "1";

        MessageEntity newData = new MessageEntity();
        newData.setContent("Nuevo contenido");

        MessageEntity updated = new MessageEntity();
        updated.setId(id);
        updated.setContent("Nuevo contenido");

        when(messageService.updateMessage(eq(id), any(MessageEntity.class))).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/" + id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(newData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.content").value("Nuevo contenido"));

        verify(messageService).updateMessage(eq(id), any(MessageEntity.class));
    }

    @Test
    void update_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";

        MessageEntity newData = new MessageEntity();
        newData.setContent("Nuevo contenido");

        when(messageService.updateMessage(eq(id), any(MessageEntity.class))).thenReturn(null);

        mockMvc.perform(put(BASE_URL + "/" + id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(newData)))
                .andExpect(status().isNotFound());

        verify(messageService).updateMessage(eq(id), any(MessageEntity.class));
    }

    // DELETE
    @Test
    void delete_cuandoExiste_debeRetornar204() throws Exception {
        String id = "1";
        when(messageService.deleteMessage(id)).thenReturn(true);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        verify(messageService).deleteMessage(id);
    }

    @Test
    void delete_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";
        when(messageService.deleteMessage(id)).thenReturn(false);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());

        verify(messageService).deleteMessage(id);
    }
}
