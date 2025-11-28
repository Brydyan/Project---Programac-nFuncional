package ec.edu.upse.backend.dto;

import lombok.Data;

@Data
public class DirectMessageDto {
    private String senderId;
    private String receiverId;
    private String content;
    // Optional attachment metadata
    private String attachmentUrl;
    private String attachmentPath;
    private String attachmentName;
    private String attachmentMime;
    private Long attachmentSize;
}