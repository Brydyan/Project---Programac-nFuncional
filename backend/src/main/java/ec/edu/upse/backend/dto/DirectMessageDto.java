package ec.edu.upse.backend.dto;

import lombok.Data;

@Data
public class DirectMessageDto {
    private String senderId;
    private String receiverId;
    private String content;
}