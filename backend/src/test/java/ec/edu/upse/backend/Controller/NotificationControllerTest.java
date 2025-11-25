package ec.edu.upse.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.upse.backend.Entity.NotificationEntity;
import ec.edu.upse.backend.Service.NotificationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/app/v1/notification";

    @Test
    void createNotification_debeRetornar200YNotificacionCreada() throws Exception {
        NotificationEntity notif = new NotificationEntity();
        notif.setId("1");
        notif.setUserId("u1");
        notif.setMessageId("m1");

        when(notificationService.save(any(NotificationEntity.class))).thenReturn(notif);

        mockMvc.perform(post(BASE_URL)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(notif)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.userId").value("u1"));

        verify(notificationService).save(any(NotificationEntity.class));
    }

    @Test
    void getAllNotifications_debeRetornar200YListaDeNotificaciones() throws Exception {
        NotificationEntity n1 = new NotificationEntity();
        n1.setId("1");

        NotificationEntity n2 = new NotificationEntity();
        n2.setId("2");

        List<NotificationEntity> list = Arrays.asList(n1, n2);

        when(notificationService.getAll()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(notificationService).getAll();
    }

    @Test
    void getNotificationById_cuandoExiste_debeRetornar200() throws Exception {
        NotificationEntity notif = new NotificationEntity();
        notif.setId("1");

        when(notificationService.getById("1")).thenReturn(Optional.of(notif));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));

        verify(notificationService).getById("1");
    }

    @Test
    void getNotificationById_cuandoNoExiste_debeRetornar404() throws Exception {
        when(notificationService.getById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNotificationsByUser_debeRetornar200() throws Exception {
        NotificationEntity n1 = new NotificationEntity();
        n1.setUserId("u1");

        NotificationEntity n2 = new NotificationEntity();
        n2.setUserId("u1");

        List<NotificationEntity> list = Arrays.asList(n1, n2);

        when(notificationService.getByUser("u1")).thenReturn(list);

        mockMvc.perform(get(BASE_URL + "/user/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(notificationService).getByUser(anyString());
    }

    @Test
    void deleteNotification_cuandoExiste_debeRetornar204() throws Exception {
        when(notificationService.delete("1")).thenReturn(true);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(notificationService).delete("1");
    }

    @Test
    void deleteNotification_cuandoNoExiste_debeRetornar404() throws Exception {
        when(notificationService.delete("99")).thenReturn(false);

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound());

        verify(notificationService).delete("99");
    }
}
