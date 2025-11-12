package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.MessageEntity;
import ec.edu.upse.backend.Repository.MessageRepository;
@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;
    // CREATE
    public MessageEntity save(MessageEntity message) {
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
            msg.setContent(newData.getContent());
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
}
