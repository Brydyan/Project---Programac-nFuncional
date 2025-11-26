package ec.edu.upse.backend.Services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    void save_conDatosValidos_debeGuardarYRetornarUsuario() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("Juan_123"); // username válido
        user.setEmail("juan@example.com"); // email válido
        user.setPassword("Password123!"); // contraseña válida (mínimo 8, mayúscula, número, especial)

        // después de normalizar debería quedar en minúsculas
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.save(user);

        assertNotNull(result);
        assertEquals("juan_123", result.getUsername()); // normalizado a minúsculas
        assertEquals("juan@example.com", result.getEmail()); // normalizado
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void save_passwordIsHashed() {
        UserEntity user = new UserEntity();
        user.setId("99");
        user.setUsername("HashTester");
        user.setEmail("hash@test.com");
        user.setPassword("Abcde1!@");

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.save(user);

        assertNotNull(result.getPassword());
        assertNotEquals("Abcde1!@", result.getPassword());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("Abcde1!@", result.getPassword()));

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void save_conUsernameInvalido_debeLanzarExcepcion() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("   "); // inválido
        user.setEmail("juan@example.com"); // válido

        assertThrows(IllegalArgumentException.class, () -> {
            userService.save(user);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void save_conEmailInvalido_debeLanzarExcepcion() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("Juan_123"); // válido
        user.setEmail("correo-sin-arroba"); // inválido

        assertThrows(IllegalArgumentException.class, () -> {
            userService.save(user);
        });

        verify(userRepository, never()).save(any());
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
        newData.setUsername("Nuevo_Usuario"); // válido
        newData.setDisplayName("Nuevo Nombre");
        newData.setEmail("nuevo@example.com"); // válido
        newData.setStatus("online");

        Map<String, Object> newPref = new HashMap<>();
        newPref.put("theme", "light");
        newData.setPreferences(newPref);

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.updateUser(id, newData);

        assertNotNull(result);
        // username / email deben salir normalizados (minúsculas, trim, etc.)
        assertEquals("nuevo_usuario", result.getUsername());
        assertEquals("Nuevo Nombre", result.getDisplayName());
        assertEquals("nuevo@example.com", result.getEmail());
        assertEquals("online", result.getStatus());
        assertEquals(newPref, result.getPreferences());

        verify(userRepository).findById(id);
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_cuandoExisteYUsernameInvalido_debeLanzarExcepcion() {
        String id = "1";

        UserEntity existing = new UserEntity();
        existing.setId(id);
        existing.setUsername("viejo");
        existing.setEmail("viejo@example.com");

        UserEntity newData = new UserEntity();
        newData.setUsername("  "); // inválido
        newData.setEmail("nuevo@example.com"); // válido

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(id, newData);
        });

        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_cuandoExisteYEmailInvalido_debeLanzarExcepcion() {
        String id = "1";

        UserEntity existing = new UserEntity();
        existing.setId(id);
        existing.setUsername("viejo");
        existing.setEmail("viejo@example.com");

        UserEntity newData = new UserEntity();
        newData.setUsername("Nuevo_Usuario"); // válido
        newData.setEmail("correo-invalido"); // inválido

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(id, newData);
        });

        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
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

    @Test
    void updateUserPhoto_cuandoExiste_debeActualizarYRetornarUsuario() {
        String id = "10";
        UserEntity existing = new UserEntity();
        existing.setId(id);
        existing.setUsername("mario");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.updateUserPhoto(id, "https://firebase.storage/avatar.jpg", "users/10/avatar.jpg");

        assertNotNull(result);
        assertEquals("https://firebase.storage/avatar.jpg", result.getPhotoUrl());
        assertEquals("users/10/avatar.jpg", result.getPhotoPath());
        verify(userRepository).findById(id);
        verify(userRepository).save(any(UserEntity.class));
    }
}