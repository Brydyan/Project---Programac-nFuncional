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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;

import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Repository.UserRepository;
import ec.edu.upse.backend.Service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // CREATE
    @Test
    void save_debeGuardarYRetornarUsuario() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("juan");

        when(userRepository.save(user)).thenReturn(user);

        UserEntity result = userService.save(user);

        assertNotNull(result);
        assertEquals("juan", result.getUsername());
        verify(userRepository).save(user);
    }

    // READ - getAllUsers
    @Test
    void getAllUsers_debeRetornarListaDeUsuarios() {
        UserEntity u1 = new UserEntity();
        u1.setId("1");
        u1.setUsername("juan");

        UserEntity u2 = new UserEntity();
        u2.setId("2");
        u2.setUsername("maria");

        List<UserEntity> list = Arrays.asList(u1, u2);
        when(userRepository.findAll()).thenReturn(list);

        List<UserEntity> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("juan", result.get(0).getUsername());
        assertEquals("maria", result.get(1).getUsername());
        verify(userRepository).findAll();
    }

    // READ - getUserById
    @Test
    void getUserById_cuandoExiste_debeRetornarOptionalConUsuario() {
        String id = "1";
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername("juan");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.getUserById(id);

        assertTrue(result.isPresent());
        assertEquals("juan", result.get().getUsername());
        verify(userRepository).findById(id);
    }

    @Test
    void getUserById_cuandoNoExiste_debeRetornarOptionalVacio() {
        String id = "99";
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.getUserById(id);

        assertFalse(result.isPresent());
        verify(userRepository).findById(id);
    }

    // READ - getUserByUsername
    @Test
    void getUserByUsername_cuandoExiste_debeRetornarOptionalConUsuario() {
        String username = "juan";
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.getUserByUsername(username);

        assertTrue(result.isPresent());
        assertEquals("juan", result.get().getUsername());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void getUserByUsername_cuandoNoExiste_debeRetornarOptionalVacio() {
        String username = "desconocido";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.getUserByUsername(username);

        assertFalse(result.isPresent());
        verify(userRepository).findByUsername(username);
    }

    // UPDATE
    @Test
    void updateUser_cuandoExiste_debeActualizarCamposYGuardar() {
        String id = "1";

        UserEntity existing = new UserEntity();
        existing.setId(id);
        existing.setUsername("viejo");
        existing.setDisplayName("Viejo Nombre");
        existing.setEmail("viejo@example.com");
        existing.setStatus("offline");

        Map<String, Object> oldPref = new HashMap<>();
        oldPref.put("theme", "dark");
        existing.setPreferences(oldPref);

        UserEntity newData = new UserEntity();
        newData.setUsername("nuevo");
        newData.setDisplayName("Nuevo Nombre");
        newData.setEmail("nuevo@example.com");
        newData.setStatus("online");

        Map<String, Object> newPref = new HashMap<>();
        newPref.put("theme", "light");
        newData.setPreferences(newPref);

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.updateUser(id, newData);

        assertNotNull(result);
        assertEquals("nuevo", result.getUsername());
        assertEquals("Nuevo Nombre", result.getDisplayName());
        assertEquals("nuevo@example.com", result.getEmail());
        assertEquals("online", result.getStatus());
        assertEquals(newPref, result.getPreferences());

        verify(userRepository).findById(id);
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_cuandoNoExiste_debeRetornarNull() {
        String id = "99";
        UserEntity newData = new UserEntity();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        UserEntity result = userService.updateUser(id, newData);

        assertNull(result);
        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }

    // DELETE
    @Test
    void deleteUser_cuandoExiste_debeBorrarYRetornarTrue() {
        String id = "1";
        when(userRepository.existsById(id)).thenReturn(true);

        boolean result = userService.deleteUser(id);

        assertTrue(result);
        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_cuandoNoExiste_noBorraYRetornaFalse() {
        String id = "99";
        when(userRepository.existsById(id)).thenReturn(false);

        boolean result = userService.deleteUser(id);

        assertFalse(result);
        verify(userRepository).existsById(id);
        verify(userRepository, never()).deleteById(anyString());
    }
}