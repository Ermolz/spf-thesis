package com.example.freelance.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderEmail;
    private String text;
    private String attachmentPath;
    private String attachmentName;
    private Boolean isRead;
    private Instant createdAt;
}

