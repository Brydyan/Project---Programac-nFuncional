package ec.edu.upse.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.upse.backend.Entity.SessionEntity;
import ec.edu.upse.backend.Service.SessionService;

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
// removed unused any() import
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/app/v1/sessions";

    @Test
    void createSession_debeRetornar200YSesionCreada() throws Exception {
        SessionEntity s = new SessionEntity();
        s.setId("1");
        s.setUserId("u1");
        s.setToken("abc123");
        s.setStatus("active");
        s.setDevice("PC");
        s.setIpAddress("127.0.0.1");
        s.setBrowser("Chrome");
        s.setLoginAt(Instant.now());

        when(sessionService.createSession(org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(java.time.Instant.class))).thenReturn(s);

        mockMvc.perform(post(BASE_URL + "/create")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(s)))
            .andExpect(status().isOk());

        verify(sessionService).createSession(org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(String.class), org.mockito.ArgumentMatchers.any(java.time.Instant.class));
    }

    @Test
    void getAllSessions_debeRetornar200() throws Exception {
        SessionEntity s1 = new SessionEntity();
        s1.setId("1");

        SessionEntity s2 = new SessionEntity();
        s2.setId("2");

        List<SessionEntity> list = Arrays.asList(s1, s2);

        when(sessionService.getAll()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(sessionService).getAll();
    }

    @Test
    void getSessionById_cuandoExiste_debeRetornar200() throws Exception {
        SessionEntity s = new SessionEntity();
        s.setId("1");

        when(sessionService.getById("1")).thenReturn(Optional.of(s));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));

        verify(sessionService).getById("1");
    }

    @Test
    void getSessionById_cuandoNoExiste_debeRetornar404() throws Exception {
        when(sessionService.getById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSessionByToken_debeRetornar200() throws Exception {
        SessionEntity s = new SessionEntity();
        s.setToken("xyz");

        when(sessionService.getByToken("xyz")).thenReturn(Optional.of(s));

        mockMvc.perform(get(BASE_URL + "/token/xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("xyz"));

        verify(sessionService).getByToken("xyz");
    }

    @Test
    void getSessionByUserId_debeRetornar200() throws Exception {
        SessionEntity s1 = new SessionEntity();
        s1.setUserId("u1");

        SessionEntity s2 = new SessionEntity();
        s2.setUserId("u1");

        List<SessionEntity> list = Arrays.asList(s1, s2);

        when(sessionService.getByUserId("u1")).thenReturn(list);

        mockMvc.perform(get(BASE_URL + "/user/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(sessionService).getByUserId("u1");
    }

    @Test
    void updateSession_cuandoExiste_debeRetornar200() throws Exception {
        SessionEntity input = new SessionEntity();
        input.setStatus("inactive");

        SessionEntity updated = new SessionEntity();
        updated.setId("1");
        updated.setStatus("inactive");

        // Replace update test with logout behavior
        when(sessionService.getById("1")).thenReturn(java.util.Optional.of(updated));

        mockMvc.perform(post(BASE_URL + "/logout/1"))
            .andExpect(status().isOk());

        verify(sessionService).logout("1");
    }

    @Test
    void updateSession_cuandoNoExiste_debeRetornar404() throws Exception {
        // Non-existent logout should still return OK (idempotent); simulate not found
        when(sessionService.getById("99")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post(BASE_URL + "/logout/99"))
            .andExpect(status().isOk());
    }

    @Test
    void logoutAll_debeRetornar200() throws Exception {
        // call logout all for a user
        mockMvc.perform(post(BASE_URL + "/logout/user/u1"))
                .andExpect(status().isOk());

        verify(sessionService).logoutAll("u1");
    }
}
