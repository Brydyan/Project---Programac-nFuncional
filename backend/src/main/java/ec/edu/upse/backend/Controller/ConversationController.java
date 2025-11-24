package ec.edu.upse.backend.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.upse.backend.Service.ConversationService;
import ec.edu.upse.backend.dto.ConversationSummaryDto;

@RestController
@RequestMapping("/app/v1/conversations")
public class ConversationController {
    @Autowired
    private ConversationService conversationService;

   @GetMapping("/{userId}")
    public ResponseEntity<List<ConversationSummaryDto>> getConversations(@PathVariable String userId) {
        List<ConversationSummaryDto> result = conversationService.getConversationsForUser(userId);
        return ResponseEntity.ok(result);
    }
}
