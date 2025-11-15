package ec.edu.upse.backend.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ec.edu.upse.backend.Entity.ContactEntity;
import ec.edu.upse.backend.Repository.ContactRepository;
import ec.edu.upse.backend.Service.ContactService;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    // CREATE
    @Test
    void save_conDatosValidos_debeGuardarYRetornarContacto() {
        ContactEntity contact = new ContactEntity();
        contact.setId("1");
        contact.setUserId("user1");
        contact.setContactId("user2");
        contact.setState("Pending"); // se normaliza a "pending"
        contact.setCreatedAt(Instant.now());

        when(contactRepository.save(any(ContactEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContactEntity result = contactService.save(contact);

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("user2", result.getContactId());
        assertEquals("pending", result.getState()); // normalizado
        verify(contactRepository).save(any(ContactEntity.class));
    }

    @Test
    void save_conIdsInvalidos_debeLanzarExcepcion() {
        ContactEntity contact = new ContactEntity();
        contact.setId("1");
        contact.setUserId("user1");
        contact.setContactId("user1"); // mismo id → inválido
        contact.setState("pending");

        assertThrows(IllegalArgumentException.class, () -> {
            contactService.save(contact);
        });

        verify(contactRepository, never()).save(any());
    }

    @Test
    void save_conEstadoInvalido_debeLanzarExcepcion() {
        ContactEntity contact = new ContactEntity();
        contact.setId("1");
        contact.setUserId("user1");
        contact.setContactId("user2");
        contact.setState("otro"); // inválido

        assertThrows(IllegalArgumentException.class, () -> {
            contactService.save(contact);
        });

        verify(contactRepository, never()).save(any());
    }

    // READ - getAllContacts
    @Test
    void getAllContacts_debeRetornarListaDeContactos() {
        ContactEntity c1 = new ContactEntity();
        c1.setId("1");
        c1.setUserId("user1");
        c1.setContactId("user2");

        ContactEntity c2 = new ContactEntity();
        c2.setId("2");
        c2.setUserId("user1");
        c2.setContactId("user3");

        List<ContactEntity> list = Arrays.asList(c1, c2);
        when(contactRepository.findAll()).thenReturn(list);

        List<ContactEntity> result = contactService.getAllContacts();

        assertEquals(2, result.size());
        verify(contactRepository).findAll();
    }

    // READ - getContactById
    @Test
    void getContactById_cuandoExiste_debeRetornarOptionalConContacto() {
        String id = "1";
        ContactEntity contact = new ContactEntity();
        contact.setId(id);

        when(contactRepository.findById(id)).thenReturn(Optional.of(contact));

        Optional<ContactEntity> result = contactService.getContactById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(contactRepository).findById(id);
    }

    @Test
    void getContactById_cuandoNoExiste_debeRetornarOptionalVacio() {
        String id = "99";
        when(contactRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ContactEntity> result = contactService.getContactById(id);

        assertFalse(result.isPresent());
        verify(contactRepository).findById(id);
    }

    // READ - getContactsByUserId
    @Test
    void getContactsByUserId_debeRetornarContactosDelUsuario() {
        String userId = "user1";

        ContactEntity c1 = new ContactEntity();
        c1.setId("1");
        c1.setUserId(userId);

        List<ContactEntity> list = Arrays.asList(c1);
        when(contactRepository.findByUserId(userId)).thenReturn(list);

        List<ContactEntity> result = contactService.getContactsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(contactRepository).findByUserId(userId);
    }

    // UPDATE - updateContactState
    @Test
    void updateContactState_cuandoExisteYEstadoValido_debeActualizar() {
        String id = "1";

        ContactEntity existing = new ContactEntity();
        existing.setId(id);
        existing.setState("pending");

        when(contactRepository.findById(id)).thenReturn(Optional.of(existing));
        when(contactRepository.save(any(ContactEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContactEntity result = contactService.updateContactState(id, "Accepted");

        assertNotNull(result);
        assertEquals("accepted", result.getState());
        verify(contactRepository).findById(id);
        verify(contactRepository).save(existing);
    }

    @Test
    void updateContactState_cuandoExisteYEstadoInvalido_debeLanzarExcepcion() {
        String id = "1";

        ContactEntity existing = new ContactEntity();
        existing.setId(id);
        existing.setState("pending");

        when(contactRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> {
            contactService.updateContactState(id, "otro");
        });

        verify(contactRepository).findById(id);
        verify(contactRepository, never()).save(any());
    }

    @Test
    void updateContactState_cuandoNoExiste_debeRetornarNull() {
        String id = "99";
        when(contactRepository.findById(id)).thenReturn(Optional.empty());

        ContactEntity result = contactService.updateContactState(id, "accepted");

        assertNull(result);
        verify(contactRepository).findById(id);
        verify(contactRepository, never()).save(any());
    }

    // DELETE
    @Test
    void deleteContact_cuandoExiste_debeBorrarYRetornarTrue() {
        String id = "1";
        when(contactRepository.existsById(id)).thenReturn(true);

        boolean result = contactService.deleteContact(id);

        assertTrue(result);
        verify(contactRepository).existsById(id);
        verify(contactRepository).deleteById(id);
    }

    @Test
    void deleteContact_cuandoNoExiste_noBorraYRetornaFalse() {
        String id = "99";
        when(contactRepository.existsById(id)).thenReturn(false);

        boolean result = contactService.deleteContact(id);

        assertFalse(result);
        verify(contactRepository).existsById(id);
        verify(contactRepository, never()).deleteById(anyString());
    }
}