package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.ContactEntity;
import ec.edu.upse.backend.Domain.MessageValidator;
import ec.edu.upse.backend.Entity.MessageEntity;
import ec.edu.upse.backend.Repository.ContactRepository;
import ec.edu.upse.backend.Repository.MessageRepository;
@Service
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // CREATE
    public MessageEntity save(MessageEntity message) {
        // usamos la función pura antes de guardar
        if (!MessageValidator.esContenidoValido(message.getContent())) {
            throw new IllegalArgumentException("El contenido del mensaje no es válido");
        }

        // opcional: normalizar antes de guardar
        message.setContent(MessageValidator.normalizarContenido(message.getContent()));

        return messageRepository.save(message);
    }

    // READ
    public List<MessageEntity> getAllMessages() {
        return messageRepository.findAll();
    }

    public Optional<MessageEntity> getMessageById(String id) {
        return messageRepository.findById(id);
    }

    public List<MessageEntity> getMessagesBySender(String senderId) {
        return messageRepository.findBySenderId(senderId);
    }

    public List<MessageEntity> getMessagesByReceiver(String receiverId) {
        return messageRepository.findByReceiverId(receiverId);
    }

    public List<MessageEntity> getMessagesByChannel(String channelId) {
        return messageRepository.findByChannelId(channelId);
    }

    // UPDATE
    public MessageEntity updateMessage(String id, MessageEntity newData) {
        Optional<MessageEntity> aux = messageRepository.findById(id);
        if (aux.isPresent()) {
            MessageEntity msg = aux.get();

            if (!MessageValidator.esContenidoValido(newData.getContent())) {
                throw new IllegalArgumentException("El contenido del mensaje no es válido");
            }

            msg.setContent(MessageValidator.normalizarContenido(newData.getContent()));
            msg.setEdited(true);
            return messageRepository.save(msg);
        }
        return null;
    }

    // DELETE
    public boolean deleteMessage(String id) {
        if (messageRepository.existsById(id)) {
            messageRepository.deleteById(id);
            return true;
        }
        return false;
    }

      //  conversación 1 a 1 (ida y vuelta) ordenada por fecha
    public List<MessageEntity> getDirectConversation(String userId, String contactId) {
        return messageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
                        userId, contactId,
                        contactId, userId
                );
    }

   //  enviar mensaje 1 a 1
public MessageEntity sendDirect(String senderId, String receiverId, String content) {
    if (!MessageValidator.esContenidoValido(content)) {
        throw new IllegalArgumentException("El contenido del mensaje no es válido");
    }

    // garantizamos que exista el contacto
    ensureContactExists(senderId, receiverId);

    // Crear mensaje
    MessageEntity message = new MessageEntity();
    message.setSenderId(senderId);
    message.setReceiverId(receiverId);
    message.setContent(MessageValidator.normalizarContenido(content));
    message.setEdited(false);
    message.setDeleted(false);
    message.setTimestamp(java.time.Instant.now());

    MessageEntity saved = messageRepository.save(message);

    // 1) ID de la conversación (mismo criterio que en el front)
    String convId = buildConversationId(senderId, receiverId);

    // 2) Notificar al hilo directo (chat abierto)
    messagingTemplate.convertAndSend(
            "/topic/direct." + convId,
            saved
    );

    // 3) 🆕 Notificar la bandeja del receptor (lista de conversaciones)
    messagingTemplate.convertAndSend(
            "/topic/inbox." + receiverId,
            saved
    );

    return saved;
}

    private void ensureContactExists(String userId, String contactId) {
    // ¿Ya existe relación userId -> contactId?
    boolean existsForward = contactRepository.findByUserId(userId).stream()
            .anyMatch(c -> contactId.equals(c.getContactId()));

    // ¿O en sentido contrario contactId -> userId?
    boolean existsBackward = contactRepository.findByUserId(contactId).stream()
            .anyMatch(c -> userId.equals(c.getContactId()));

    if (existsForward || existsBackward) {
        return; // ya hay contacto registrado, no hacemos nada
    }

    // Si no existe, creamos uno "accepted"
    ContactEntity c = new ContactEntity();
    c.setUserId(userId);
    c.setContactId(contactId);
    c.setState("accepted");  // válido según ContactValidator

    contactRepository.save(c);
}


   private String buildConversationId(String userA, String userB) {
        return (userA.compareTo(userB) < 0)
                ? userA + "_" + userB
                : userB + "_" + userA;
    }


}