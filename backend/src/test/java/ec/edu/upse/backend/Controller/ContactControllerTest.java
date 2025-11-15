package ec.edu.upse.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.upse.backend.Entity.ContactEntity;
import ec.edu.upse.backend.Service.ContactService;
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
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false) // desactivar filtros de seguridad
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/app/v1/contacts";

    // CREATE
    @Test
    void create_debeRetornar200YContactoCreado() throws Exception {
        ContactEntity contact = new ContactEntity();
        contact.setId("1");
        contact.setUserId("user1");
        contact.setContactId("user2");
        contact.setState("pending");
        contact.setCreatedAt(Instant.now());

        when(contactService.save(any(ContactEntity.class))).thenReturn(contact);

        mockMvc.perform(post(BASE_URL)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.contactId").value("user2"))
                .andExpect(jsonPath("$.state").value("pending"));

        verify(contactService).save(any(ContactEntity.class));
    }

    // READ - getAll
    @Test
    void getAll_debeRetornar200YListaDeContactos() throws Exception {
        ContactEntity c1 = new ContactEntity();
        c1.setId("1");
        c1.setUserId("user1");
        c1.setContactId("user2");

        ContactEntity c2 = new ContactEntity();
        c2.setId("2");
        c2.setUserId("user1");
        c2.setContactId("user3");

        List<ContactEntity> list = Arrays.asList(c1, c2);

        when(contactService.getAllContacts()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        verify(contactService).getAllContacts();
    }

    // READ - getById
    @Test
    void getById_cuandoExiste_debeRetornar200YContacto() throws Exception {
        String id = "1";

        ContactEntity contact = new ContactEntity();
        contact.setId(id);
        contact.setUserId("user1");
        contact.setContactId("user2");

        when(contactService.getContactById(id)).thenReturn(Optional.of(contact));

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.contactId").value("user2"));

        verify(contactService).getContactById(id);
    }

    @Test
    void getById_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";
        when(contactService.getContactById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());

        verify(contactService).getContactById(id);
    }

    // DELETE
    @Test
    void delete_cuandoExiste_debeRetornar204() throws Exception {
        String id = "1";
        when(contactService.deleteContact(id)).thenReturn(true);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        verify(contactService).deleteContact(id);
    }

    @Test
    void delete_cuandoNoExiste_debeRetornar404() throws Exception {
        String id = "99";
        when(contactService.deleteContact(id)).thenReturn(false);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());

        verify(contactService).deleteContact(id);
    }
}
