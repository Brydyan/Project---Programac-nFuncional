package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.ChannelMsgEntity;
import ec.edu.upse.backend.Repository.ChannelMsgRepository;

@Service
public class ChannelMsgService {
    /**
     * Repositorio de mensajes de canal
     * 
     */
    @Autowired
    private ChannelMsgRepository channelMsgRepository;

    // CREATE
    public ChannelMsgEntity save(ChannelMsgEntity message) {
        return channelMsgRepository.save(message);
    }

    // READ
    public List<ChannelMsgEntity> getAllMessages() {
        return channelMsgRepository.findAll();
    }

    public Optional<ChannelMsgEntity> getMessageById(String id) {
        return channelMsgRepository.findById(id);
    }

    public Optional<ChannelMsgEntity> getMessageByMessageId(String messageId) {
        return channelMsgRepository.findByMessageId(messageId);
    }

    public List<ChannelMsgEntity> getMessagesByChannel(String channelId) {
        return channelMsgRepository.findByChannel(channelId);
    }

    public List<ChannelMsgEntity> getMessagesBySender(String senderId) {
        return channelMsgRepository.findBySenderId(senderId);
    }

    public List<ChannelMsgEntity> getMessagesByStatus(String status) {
        return channelMsgRepository.findByStatus(status);
    }

    // UPDATE
    public ChannelMsgEntity updateMessage(String id, ChannelMsgEntity newData) {
        Optional<ChannelMsgEntity> aux = channelMsgRepository.findById(id);
        if (aux.isPresent()) {
            ChannelMsgEntity msg = aux.get();
            msg.setMessageContent(newData.getMessageContent());
            msg.setStatus(newData.getStatus());
            return channelMsgRepository.save(msg);
        }
        return null;
    }

    // DELETE
    public boolean deleteMessage(String id) {
        if (channelMsgRepository.existsById(id)) {
            channelMsgRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
