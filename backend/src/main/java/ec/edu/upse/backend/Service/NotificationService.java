package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.NotificationEntity;
import ec.edu.upse.backend.Repository.NotificationRepository;
@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public NotificationEntity save(NotificationEntity notification) {
        return notificationRepository.save(notification);
    }

    public List<NotificationEntity> getAll() {
        return notificationRepository.findAll();
    }

    public Optional<NotificationEntity> getById(String id) {
        return notificationRepository.findById(id);
    }

    public List<NotificationEntity> getByUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public boolean delete(String id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
