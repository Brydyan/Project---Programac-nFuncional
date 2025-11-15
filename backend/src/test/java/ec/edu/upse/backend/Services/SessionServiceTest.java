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

import ec.edu.upse.backend.Entity.SessionEntity;
import ec.edu.upse.backend.Repository.SessionRepository;
import ec.edu.upse.backend.Service.SessionService;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionService sessionService;

    // CREATE
    @Test
    void save_conDatosValidos_debeGuardarYRetornarSesion() {
        SessionEntity session = new SessionEntity();
        session.setId("1");
        session.setUserId("user1");
        session.setToken("token-123");
        session.setStatus("ACTIVE"); // se normaliza a "active"
        session.setDevice("PC");
        session.setIpAddress("127.0.0.1");
        session.setLocation("EC");
        session.setBrowser("Chrome");
        session.setHoraFecha(Instant.now());

        when(sessionRepository.save(any(SessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SessionEntity result = sessionService.save(session);

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("token-123", result.getToken());
        assertEquals("active", result.getStatus());
        verify(sessionRepository).save(any(SessionEntity.class));
    }

    @Test
    void save_conUserIdInvalido_debeLanzarExcepcion() {
        SessionEntity session = new SessionEntity();
        session.setId("1");
        session.setUserId("   "); // inválido
        session.setToken("token-123");
        session.setStatus("active");

        assertThrows(IllegalArgumentException.class, () -> {
            sessionService.save(session);
        });

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void save_conTokenInvalido_debeLanzarExcepcion() {
        SessionEntity session = new SessionEntity();
        session.setId("1");
        session.setUserId("user1");
        session.setToken("   "); // inválido
        session.setStatus("active");

        assertThrows(IllegalArgumentException.class, () -> {
            sessionService.save(session);
        });

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void save_conStatusInvalido_debeLanzarExcepcion() {
        SessionEntity session = new SessionEntity();
        session.setId("1");
        session.setUserId("user1");
        session.setToken("token-123");
        session.setStatus("otro"); // inválido

        assertThrows(IllegalArgumentException.class, () -> {
            sessionService.save(session);
        });

        verify(sessionRepository, never()).save(any());
    }

    // READ - getAllSessions
    @Test
    void getAllSessions_debeRetornarListaDeSesiones() {
        SessionEntity s1 = new SessionEntity();
        s1.setId("1");
        SessionEntity s2 = new SessionEntity();
        s2.setId("2");

        List<SessionEntity> list = Arrays.asList(s1, s2);
        when(sessionRepository.findAll()).thenReturn(list);

        List<SessionEntity> result = sessionService.getAllSessions();

        assertEquals(2, result.size());
        verify(sessionRepository).findAll();
    }

    // READ - getSessionById
    @Test
    void getSessionById_cuandoExiste_debeRetornarOptionalConSesion() {
        String id = "1";
        SessionEntity s = new SessionEntity();
        s.setId(id);

        when(sessionRepository.findById(id)).thenReturn(Optional.of(s));

        Optional<SessionEntity> result = sessionService.getSessionById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(sessionRepository).findById(id);
    }

    @Test
    void getSessionById_cuandoNoExiste_debeRetornarOptionalVacio() {
        String id = "99";
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        Optional<SessionEntity> result = sessionService.getSessionById(id);

        assertFalse(result.isPresent());
        verify(sessionRepository).findById(id);
    }

    // READ - getSessionByToken
    @Test
    void getSessionByToken_debeRetornarOptionalConSesion() {
        String token = "token-123";
        SessionEntity s = new SessionEntity();
        s.setId("1");
        s.setToken(token);

        when(sessionRepository.findByToken(token)).thenReturn(Optional.of(s));

        Optional<SessionEntity> result = sessionService.getSessionByToken(token);

        assertTrue(result.isPresent());
        assertEquals(token, result.get().getToken());
        verify(sessionRepository).findByToken(token);
    }

    // READ - getSessionsByUserId
    @Test
    void getSessionsByUserId_debeRetornarSesionesDelUsuario() {
        String userId = "user1";

        SessionEntity s = new SessionEntity();
        s.setId("1");
        s.setUserId(userId);

        List<SessionEntity> list = List.of(s);
        when(sessionRepository.findByUserId(userId)).thenReturn(list);

        List<SessionEntity> result = sessionService.getSessionsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(sessionRepository).findByUserId(userId);
    }

    // UPDATE - updateSession
    @Test
    void updateSession_cuandoExisteYStatusValido_debeActualizarYGuardar() {
        String id = "1";

        SessionEntity existing = new SessionEntity();
        existing.setId(id);
        existing.setStatus("inactive");
        existing.setDevice("PC");
        existing.setIpAddress("127.0.0.1");
        existing.setLocation("EC");
        existing.setBrowser("Chrome");

        SessionEntity newData = new SessionEntity();
        newData.setStatus("ACTIVE");
        newData.setDevice("MOBILE");
        newData.setIpAddress("192.168.0.1");
        newData.setLocation("US");
        newData.setBrowser("Firefox");

        when(sessionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sessionRepository.save(any(SessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SessionEntity result = sessionService.updateSession(id, newData);

        assertNotNull(result);
        assertEquals("active", result.getStatus());
        assertEquals("MOBILE", result.getDevice());
        assertEquals("192.168.0.1", result.getIpAddress());
        assertEquals("US", result.getLocation());
        assertEquals("Firefox", result.getBrowser());

        verify(sessionRepository).findById(id);
        verify(sessionRepository).save(existing);
    }

    @Test
    void updateSession_cuandoExisteYStatusInvalido_debeLanzarExcepcion() {
        String id = "1";

        SessionEntity existing = new SessionEntity();
        existing.setId(id);
        existing.setStatus("inactive");

        SessionEntity newData = new SessionEntity();
        newData.setStatus("otro"); // inválido

        when(sessionRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> {
            sessionService.updateSession(id, newData);
        });

        verify(sessionRepository).findById(id);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void updateSession_cuandoNoExiste_debeRetornarNull() {
        String id = "99";
        SessionEntity newData = new SessionEntity();

        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        SessionEntity result = sessionService.updateSession(id, newData);

        assertNull(result);
        verify(sessionRepository).findById(id);
        verify(sessionRepository, never()).save(any());
    }

    // DELETE
    @Test
    void deleteSession_cuandoExiste_debeBorrarYRetornarTrue() {
        String id = "1";
        when(sessionRepository.existsById(id)).thenReturn(true);

        boolean result = sessionService.deleteSession(id);

        assertTrue(result);
        verify(sessionRepository).existsById(id);
        verify(sessionRepository).deleteById(id);
    }

    @Test
    void deleteSession_cuandoNoExiste_noBorraYRetornaFalse() {
        String id = "99";
        when(sessionRepository.existsById(id)).thenReturn(false);

        boolean result = sessionService.deleteSession(id);

        assertFalse(result);
        verify(sessionRepository).existsById(id);
        verify(sessionRepository, never()).deleteById(anyString());
    }

    // DELETE all by user
    @Test
    void deleteSessionsByUserId_debeBorrarTodasLasSesionesDelUsuario() {
        String userId = "user1";

        SessionEntity s1 = new SessionEntity();
        s1.setId("1");
        s1.setUserId(userId);

        SessionEntity s2 = new SessionEntity();
        s2.setId("2");
        s2.setUserId(userId);

        List<SessionEntity> sessions = Arrays.asList(s1, s2);

        when(sessionRepository.findByUserId(userId)).thenReturn(sessions);

        sessionService.deleteSessionsByUserId(userId);

        verify(sessionRepository).findByUserId(userId);
        verify(sessionRepository).deleteAll(sessions);
    }
}