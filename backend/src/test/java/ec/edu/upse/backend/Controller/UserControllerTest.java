package ec.edu.upse.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/app/v1/user";

    @Test
    void createUser_debeRetornar200YUsuarioCreado() throws Exception {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("juan");
        user.setEmail("juan@example.com");

        when(userService.save(any(UserEntity.class))).thenReturn(user);

        mockMvc.perform(post(BASE_URL)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("juan"));

        verify(userService).save(any(UserEntity.class));
    }

    @Test
    void getAllUsers_debeRetornar200YListaDeUsuarios() throws Exception {
        UserEntity u1 = new UserEntity();
        u1.setId("1");

        UserEntity u2 = new UserEntity();
        u2.setId("2");

        List<UserEntity> list = Arrays.asList(u1, u2);

        when(userService.getAllUsers()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_cuandoExiste_debeRetornar200() throws Exception {
        UserEntity user = new UserEntity();
        user.setId("1");

        when(userService.getUserById("1")).thenReturn(Optional.of(user));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));

        verify(userService).getUserById("1");
    }

    @Test
    void getUserById_cuandoNoExiste_debeRetornar404() throws Exception {
        when(userService.getUserById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsername_cuandoExiste_debeRetornar200() throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername("maria");

        when(userService.getUserByUsername("maria")).thenReturn(Optional.of(user));

        mockMvc.perform(get(BASE_URL + "/username/maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("maria"));

        verify(userService).getUserByUsername(anyString());
    }

    @Test
    void updateUser_cuandoExiste_debeRetornar200() throws Exception {
        UserEntity input = new UserEntity();
        input.setUsername("nuevo");

        UserEntity updated = new UserEntity();
        updated.setId("1");
        updated.setUsername("nuevo");

        when(userService.updateUser(eq("1"), any(UserEntity.class))).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nuevo"));
    }

    @Test
    void updateUser_cuandoNoExiste_debeRetornar404() throws Exception {
        UserEntity input = new UserEntity();
        input.setUsername("nuevo");

        when(userService.updateUser(eq("99"), any(UserEntity.class))).thenReturn(null);

        mockMvc.perform(put(BASE_URL + "/99")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_cuandoExiste_debeRetornar204() throws Exception {
        when(userService.deleteUser("1")).thenReturn(true);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_cuandoNoExiste_debeRetornar404() throws Exception {
        when(userService.deleteUser("99")).thenReturn(false);

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }
}
