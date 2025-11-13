package com.example.freelance.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {
    private Long projectId;
    private Long assignmentId;
    private Long freelancerId;

    public boolean isValid() {
        if (assignmentId != null) {
            return true;
        }
        if (projectId != null) {
            return freelancerId != null;
        }
        return false;
    }
}

