package ec.edu.upse.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.upse.backend.Entity.PresenceEntity;
import ec.edu.upse.backend.Service.PresenceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PresenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class PresenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PresenceService presenceService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/app/v1/presence";

    @Test
    void createPresence_debeRetornar200YPresenceCreada() throws Exception {
        PresenceEntity p = new PresenceEntity();
        p.setId("1");
        p.setUserId("u1");
        p.setStatus("online");
        p.setLastSeen(Instant.now());

        when(presenceService.save(any(PresenceEntity.class))).thenReturn(p);

        mockMvc.perform(post(BASE_URL)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.userId").value("u1"));

        verify(presenceService).save(any(PresenceEntity.class));
    }

    @Test
    void getAllPresence_debeRetornar200YLista() throws Exception {
        PresenceEntity p1 = new PresenceEntity();
        p1.setId("1");

        PresenceEntity p2 = new PresenceEntity();
        p2.setId("2");

        List<PresenceEntity> list = Arrays.asList(p1, p2);
        when(presenceService.getAll()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(presenceService).getAll();
    }

    @Test
    void getPresenceById_cuandoExiste_debeRetornar200() throws Exception {
        PresenceEntity p = new PresenceEntity();
        p.setId("1");

        when(presenceService.getById("1")).thenReturn(Optional.of(p));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));

        verify(presenceService).getById("1");
    }

    @Test
    void getPresenceById_cuandoNoExiste_debeRetornar404() throws Exception {
        when(presenceService.getById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPresenceByUser_debeRetornar200() throws Exception {
        PresenceEntity p1 = new PresenceEntity();
        p1.setUserId("u1");
        PresenceEntity p2 = new PresenceEntity();
        p2.setUserId("u1");

        List<PresenceEntity> list = Arrays.asList(p1, p2);

        when(presenceService.getByUser("u1")).thenReturn(list);

        mockMvc.perform(get(BASE_URL + "/user/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(presenceService).getByUser(anyString());
    }

    @Test
    void deletePresence_cuandoExiste_debeRetornar204() throws Exception {
        when(presenceService.delete("1")).thenReturn(true);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(presenceService).delete("1");
    }

    @Test
    void deletePresence_cuandoNoExiste_debeRetornar404() throws Exception {
        when(presenceService.delete("99")).thenReturn(false);

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound());

        verify(presenceService).delete("99");
    }
}
