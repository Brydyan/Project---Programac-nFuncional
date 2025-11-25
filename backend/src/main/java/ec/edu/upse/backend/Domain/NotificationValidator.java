package ec.edu.upse.backend.Domain;

public class NotificationValidator {

    public static boolean sonIdsValidos(String userId, String messageId) {
        if (userId == null || messageId == null)
            return false;
        if (userId.trim().isEmpty() || messageId.trim().isEmpty())
            return false;
        return true;
    }
}