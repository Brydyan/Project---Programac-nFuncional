package ec.edu.upse.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.upse.backend.Entity.ChannelEntity;
import ec.edu.upse.backend.Service.ChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ChannelController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChannelService channelService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/app/v1/channels";

    @Test
    void createChannel_debeRetornar200YCanalCreado() throws Exception {
        ChannelEntity channel = new ChannelEntity();
        channel.setId("1");
        channel.setName("general");
        channel.setType("TEXT");

        when(channelService.save(any(ChannelEntity.class))).thenReturn(channel);

        mockMvc.perform(post(BASE_URL)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(channel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("general"));

        verify(channelService).save(any(ChannelEntity.class));
    }

    @Test
    void getAllChannels_debeRetornar200YListaDeCanales() throws Exception {
        ChannelEntity c1 = new ChannelEntity();
        c1.setId("1");
        c1.setName("general");

        ChannelEntity c2 = new ChannelEntity();
        c2.setId("2");
        c2.setName("random");

        List<ChannelEntity> list = Arrays.asList(c1, c2);

        when(channelService.getAllChannels()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("general"))
                .andExpect(jsonPath("$[1].name").value("random"));

        verify(channelService).getAllChannels();
    }

    @Test
    void getChannelById_cuandoExiste_debeRetornar200YCanal() throws Exception {
        String id = "1";
        ChannelEntity channel = new ChannelEntity();
        channel.setId(id);
        channel.setName("general");

        when(channelService.getChannelById(id)).thenReturn(Optional.of(channel));

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("general"));

        verify(channelService).getChannelById(id);
    }

    @Test
    void getChannelById_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";
        when(channelService.getChannelById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());

        verify(channelService).getChannelById(id);
    }

    @Test
    void updateChannel_cuandoExiste_debeRetornar200YCanalActualizado() throws Exception {
        String id = "1";

        ChannelEntity newData = new ChannelEntity();
        newData.setName("nuevo");
        newData.setType("VOICE");

        ChannelEntity updated = new ChannelEntity();
        updated.setId(id);
        updated.setName("nuevo");
        updated.setType("VOICE");

        when(channelService.updateChannel(eq(id), any(ChannelEntity.class))).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("nuevo"))
                .andExpect(jsonPath("$.type").value("VOICE"));

        verify(channelService).updateChannel(eq(id), any(ChannelEntity.class));
    }

    @Test
    void updateChannel_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";

        ChannelEntity newData = new ChannelEntity();
        newData.setName("nuevo");

        when(channelService.updateChannel(eq(id), any(ChannelEntity.class))).thenReturn(null);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newData)))
                .andExpect(status().isNotFound());

        verify(channelService).updateChannel(eq(id), any(ChannelEntity.class));
    }

    @Test
    void deleteChannel_cuandoExiste_debeRetornar204() throws Exception {
        String id = "1";
        when(channelService.deleteChannel(id)).thenReturn(true);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        verify(channelService).deleteChannel(id);
    }

    @Test
    void deleteChannel_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";
        when(channelService.deleteChannel(id)).thenReturn(false);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());

        verify(channelService).deleteChannel(id);
    }
}
