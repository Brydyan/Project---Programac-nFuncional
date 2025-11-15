package ec.edu.upse.backend.Domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationValidatorTest {

    @Test
    void sonIdsValidos_conUserIdYMessageIdCorrectos_debeSerTrue() {
        assertTrue(NotificationValidator.sonIdsValidos("user1", "msg1"));
    }

    @Test
    void sonIdsValidos_conNullOVacios_debeSerFalse() {
        assertFalse(NotificationValidator.sonIdsValidos(null, "msg1"));
        assertFalse(NotificationValidator.sonIdsValidos("user1", null));
        assertFalse(NotificationValidator.sonIdsValidos("", "msg1"));
        assertFalse(NotificationValidator.sonIdsValidos("user1", ""));
        assertFalse(NotificationValidator.sonIdsValidos("   ", "msg1"));
    }
}