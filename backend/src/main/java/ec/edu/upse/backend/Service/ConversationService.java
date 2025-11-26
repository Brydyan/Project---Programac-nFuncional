package ec.edu.upse.backend.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.ContactEntity;
import ec.edu.upse.backend.Entity.MessageEntity;
import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Repository.ContactRepository;
import ec.edu.upse.backend.Repository.MessageRepository;
import ec.edu.upse.backend.Repository.UserRepository;
import ec.edu.upse.backend.dto.ConversationSummaryDto;

@Service
public class ConversationService {

    @Autowired 
    private ContactRepository contactRepository;

    @Autowired 
    private MessageRepository messageRepository;

    @Autowired 
    private UserRepository userRepository;

    @Autowired
    private MessageService messageService;

    public List<ConversationSummaryDto> getConversationsForUser(String userId) {
        List<ContactEntity> asOwner = contactRepository.findByUserId(userId);
        List<ContactEntity> asContact = contactRepository.findByContactId(userId);

        Map<String, ContactEntity> relations = new HashMap<>();


       for(ContactEntity c : asOwner){
            if("accepted".equalsIgnoreCase(c.getState())){
                relations.put(c.getContactId(), c);
            }
       }
    
       for(ContactEntity c: asContact){
            if("accepted".equalsIgnoreCase(c.getState())){
                relations.put(c.getUserId(), c);
            }
       }
       // Para cada contacto, buscar el último mensaje (en ambos sentidos)
       List<ConversationSummaryDto> result = new ArrayList<>();
       
       for(String otherUserId : relations.keySet()){

        MessageEntity last1 = messageRepository
                    .findTopBySenderIdAndReceiverIdOrderByTimestampDesc(userId, otherUserId);


        MessageEntity last2 = messageRepository
                    .findTopBySenderIdAndReceiverIdOrderByTimestampDesc(otherUserId, userId);

        MessageEntity last = pickLatest(last1, last2);

        String lastMeesageText = last != null ? last.getContent() : "";
        java.time.Instant lastTime = last != null ? last.getTimestamp() : null;
        //Buscar datos del usuario para mostrar nombre
        Optional<UserEntity> otherUserOpt = userRepository.findById(otherUserId);
        String displayName = otherUserOpt
                    .map(u -> u.getDisplayName() != null && !u.getDisplayName().isEmpty()
                            ? u.getDisplayName()
                            : u.getUsername())
                    .orElse("Usuario " + otherUserId); 



        String avatarUrl = otherUserOpt.map(u -> u.getPhotoUrl() == null ? "" : u.getPhotoUrl()).orElse("");

        String convId = buildConversationId(userId, otherUserId);
        long unread = 0L;
        try { unread = messageService.getUnreadCountForConversation(convId, userId); } catch(Exception e) { unread = 0L; }
        int unreadCount = (int) unread;

        ConversationSummaryDto dto = new ConversationSummaryDto(
                otherUserId,
                displayName,
                lastMeesageText,
                lastTime,
                avatarUrl,
                unreadCount
        );
        result.add(dto);
       }
       return result.stream()
            .sorted(Comparator.comparing(ConversationSummaryDto :: getLastTime,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .collect(Collectors.toList());
    }
    private MessageEntity pickLatest(MessageEntity a, MessageEntity b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.getTimestamp().isAfter(b.getTimestamp()) ? a : b;
    }

    private String buildConversationId(String a, String b) {
        return (a.compareTo(b) < 0) ? a + "_" + b : b + "_" + a;
    }
}
