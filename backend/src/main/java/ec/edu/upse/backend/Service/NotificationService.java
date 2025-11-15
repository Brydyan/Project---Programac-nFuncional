package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Domain.NotificationValidator;
import ec.edu.upse.backend.Entity.NotificationEntity;
import ec.edu.upse.backend.Repository.NotificationRepository;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    // CREATE
    public NotificationEntity save(NotificationEntity notification) {
        if (!NotificationValidator.sonIdsValidos(notification.getUserId(), notification.getMessageId())) {
            throw new IllegalArgumentException("Ids de notificación inválidos");
        }

        // createdAt ya se setea en la entidad por defecto
        return notificationRepository.save(notification);
    }

    // READ
    public List<NotificationEntity> getAll() {
        return notificationRepository.findAll();
    }

    public Optional<NotificationEntity> getById(String id) {
        return notificationRepository.findById(id);
    }

    public List<NotificationEntity> getByUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    // UPDATE: marcar como leída
    public NotificationEntity markAsRead(String id) {
        Optional<NotificationEntity> aux = notificationRepository.findById(id);
        if (aux.isPresent()) {
            NotificationEntity notif = aux.get();
            notif.setRead(true);
            return notificationRepository.save(notif);
        }
        return null;
    }

    // DELETE
    public boolean delete(String id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}