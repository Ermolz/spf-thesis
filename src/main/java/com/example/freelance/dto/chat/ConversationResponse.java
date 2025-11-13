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
public class ConversationResponse {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long assignmentId;
    private Long clientId;
    private String clientEmail;
    private Long freelancerId;
    private String freelancerEmail;
    private String freelancerDisplayName;
    private Long unreadCount;
    private Instant createdAt;
    private Instant updatedAt;
}

