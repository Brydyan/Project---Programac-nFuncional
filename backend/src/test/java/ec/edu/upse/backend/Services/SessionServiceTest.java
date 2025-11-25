package ec.edu.upse.backend.Services;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ec.edu.upse.backend.Entity.SessionEntity;
import ec.edu.upse.backend.Repository.SessionRepository;
import ec.edu.upse.backend.Service.RealtimePresenceService;
import ec.edu.upse.backend.Service.SessionService;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private RealtimePresenceService presence;

    @Mock
    private ec.edu.upse.backend.Service.IpLocationService ipLocationService;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void createSession_validData_savesAndReturnsSession() {
        when(sessionRepository.save(any(SessionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ipLocationService.getLocation(anyString())).thenReturn("Quito, Pichincha, Ecuador");

        Instant expires = Instant.now().plus(Duration.ofDays(7));
        SessionEntity result = sessionService.createSession("user1", "token-123", "PC", "127.0.0.1", "Chrome", expires);

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        assertEquals("token-123", result.getToken());
        assertEquals("active", result.getStatus());

        verify(sessionRepository).save(any(SessionEntity.class));
        verify(presence).setOnline(anyString(), anyString());
    }

    @Test
    void refreshActivity_updatesLastActivityAndRefreshesPresence() {
        SessionEntity s = new SessionEntity();
        s.setId("sid1");
        s.setUserId("user1");
        s.setLastActivity(Instant.now().minusSeconds(3600));

        when(sessionRepository.findBySessionId("sid1")).thenReturn(Optional.of(s));

        sessionService.refreshActivity("sid1");

        verify(sessionRepository).save(s);
        verify(presence).refresh("user1", s.getSessionId());
    }

    @Test
    void logout_marksSessionInactiveAndRemovesPresence() {
        SessionEntity s = new SessionEntity();
        s.setId("sid2");
        s.setUserId("user2");
        s.setSessionId("session-123");
        s.setValid(true);
        s.setStatus("active");

        when(sessionRepository.findBySessionId("sid2")).thenReturn(Optional.of(s));

        sessionService.logout("sid2");

        verify(sessionRepository).save(s);
        verify(presence).setOffline("user2", "session-123");
    }

    @Test
    void logoutAll_findsUserSessionsAndLogsOutEach() {
        String userId = "u1";
        SessionEntity s1 = new SessionEntity(); s1.setId("1"); s1.setSessionId("s1"); s1.setUserId(userId);
        SessionEntity s2 = new SessionEntity(); s2.setId("2"); s2.setSessionId("s2"); s2.setUserId(userId);

        List<SessionEntity> sessions = Arrays.asList(s1, s2);
        when(sessionRepository.findByUserId(userId)).thenReturn(sessions);

        sessionService.logoutAll(userId);

        verify(sessionRepository, times(2)).save(any(SessionEntity.class));
        verify(presence, times(2)).setOffline(anyString(), anyString());
    }

    @Test
    void getters_returnRepositoryResults() {
        SessionEntity s1 = new SessionEntity(); s1.setId("1");
        SessionEntity s2 = new SessionEntity(); s2.setId("2");

        when(sessionRepository.findAll()).thenReturn(Arrays.asList(s1, s2));
        when(sessionRepository.findById("1")).thenReturn(Optional.of(s1));
        s1.setToken("t1");
        when(sessionRepository.findByToken("t1")).thenReturn(Optional.of(s1));
        when(sessionRepository.findByUserId("u1")).thenReturn(Arrays.asList(s1));

        List<SessionEntity> all = sessionService.getAll();
        Optional<SessionEntity> byId = sessionService.getById("1");
        Optional<SessionEntity> byToken = sessionService.getByToken("t1");
        List<SessionEntity> byUser = sessionService.getByUserId("u1");

        assertEquals(2, all.size());
        assertEquals("1", byId.get().getId());
        assertEquals("t1", byToken.get().getToken());
        assertEquals(1, byUser.size());
    }
}