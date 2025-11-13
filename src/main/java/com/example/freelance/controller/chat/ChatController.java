package com.example.freelance.controller.chat;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.chat.ConversationResponse;
import com.example.freelance.dto.chat.CreateConversationRequest;
import com.example.freelance.dto.chat.MessageResponse;
import com.example.freelance.dto.chat.SendMessageRequest;
import com.example.freelance.service.chat.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Chat", description = "Messaging endpoints. Enables communication between clients and freelancers through conversations and messages.")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
public class ChatController {
    private final ChatService chatService;

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a conversation",
            description = """
                    Creates a new conversation between a client and freelancer.
                    
                    **Conversation Types:**
                    - Project-based: Created for a specific project (requires projectId and freelancerId)
                    - Assignment-based: Created for an active assignment (requires assignmentId)
                    - Direct: Created between client and freelancer (requires freelancerId)
                    
                    **Rules:**
                    - Cannot create duplicate conversations
                    - Cannot create conversation with yourself
                    - Must be associated with a project or assignment you have access to
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Conversation creation details",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateConversationRequest.class)
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Conversation successfully created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Invalid conversation data or duplicate conversation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to create this conversation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project, assignment, or freelancer not found")
    })
    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(@Valid @RequestBody CreateConversationRequest request) {
        ConversationResponse response = chatService.createConversation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get user's conversations",
            description = "Retrieves all conversations for the authenticated user, including unread message counts."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversations retrieved successfully")
    })
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations() {
        List<ConversationResponse> response = chatService.getMyConversations();
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get conversation by ID",
            description = "Retrieves a single conversation by its ID. Only participants can view the conversation.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "Conversation unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not a participant in this conversation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversationById(@PathVariable Long id) {
        ConversationResponse response = chatService.getConversationById(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Send a message",
            description = """
                    Sends a message in a conversation. Only conversation participants can send messages.
                    
                    **Message Features:**
                    - Text messages are required
                    - Optional file attachments (max 10MB)
                    - Messages are marked as unread for the recipient
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Message details",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SendMessageRequest.class)
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Message successfully sent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not a participant in this conversation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = chatService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get conversation messages",
            description = """
                    Retrieves paginated message history for a conversation.
                    
                    **Pagination:**
                    - Default page size: 50
                    - Default sort: createdAt DESC (newest first)
                    - Messages are returned in reverse chronological order
                    """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "conversationId", description = "Conversation unique identifier", required = true, example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Page size", example = "50"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not a participant in this conversation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getConversationMessages(
            @PathVariable Long conversationId,
            @PageableDefault(size = 50, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<MessageResponse> response = chatService.getConversationMessages(conversationId, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }
}

