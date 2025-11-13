package com.example.freelance.service.chat;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.chat.Conversation;
import com.example.freelance.domain.chat.Message;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.chat.ConversationResponse;
import com.example.freelance.dto.chat.CreateConversationRequest;
import com.example.freelance.dto.chat.MessageResponse;
import com.example.freelance.dto.chat.SendMessageRequest;
import com.example.freelance.mapper.chat.ConversationMapper;
import com.example.freelance.mapper.chat.MessageMapper;
import com.example.freelance.repository.assignment.AssignmentRepository;
import com.example.freelance.repository.chat.ConversationRepository;
import com.example.freelance.repository.chat.MessageRepository;
import com.example.freelance.repository.project.ProjectRepository;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.repository.user.UserRepository;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final AssignmentRepository assignmentRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();

        if (!request.isValid()) {
            throw new BadRequestException("Either projectId or assignmentId must be provided", "INVALID_CONVERSATION_REQUEST");
        }

        Project project = null;
        Assignment assignment = null;
        ClientProfile client = null;
        FreelancerProfile freelancer = null;

        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new NotFoundException("Project", request.getProjectId().toString()));

            if (request.getFreelancerId() == null) {
                throw new BadRequestException("Freelancer ID is required when creating conversation for project", "FREELANCER_ID_REQUIRED");
            }

            client = project.getClient();
            freelancer = freelancerProfileRepository.findById(request.getFreelancerId())
                    .orElseThrow(() -> new NotFoundException("FreelancerProfile", request.getFreelancerId().toString()));

            if (client.getUser().getId().equals(userPrincipal.getId()) && 
                freelancer.getUser().getId().equals(userPrincipal.getId())) {
                throw new BadRequestException("Cannot create conversation with yourself", "CANNOT_CHAT_WITH_YOURSELF");
            }

            if (!client.getUser().getId().equals(userPrincipal.getId()) && 
                !freelancer.getUser().getId().equals(userPrincipal.getId())) {
                throw new ForbiddenException("Access denied to this project", "ACCESS_DENIED");
            }
        } else if (request.getAssignmentId() != null) {
            assignment = assignmentRepository.findById(request.getAssignmentId())
                    .orElseThrow(() -> new NotFoundException("Assignment", request.getAssignmentId().toString()));

            client = assignment.getProject().getClient();
            freelancer = assignment.getFreelancer();

            if (!client.getUser().getId().equals(userPrincipal.getId()) &&
                !freelancer.getUser().getId().equals(userPrincipal.getId())) {
                throw new ForbiddenException("Access denied to this assignment", "ACCESS_DENIED");
            }
        }

        Optional<Conversation> existingConversation = conversationRepository
                .findByProjectOrAssignmentAndParticipants(
                        request.getProjectId(),
                        request.getAssignmentId(),
                        client.getId(),
                        freelancer.getId()
                );

        if (existingConversation.isPresent()) {
            return mapConversationToResponse(existingConversation.get(), userPrincipal.getId());
        }

        Conversation conversation = new Conversation();
        conversation.setProject(project);
        conversation.setAssignment(assignment);
        conversation.setClient(client);
        conversation.setFreelancer(freelancer);

        conversation = conversationRepository.save(conversation);
        return mapConversationToResponse(conversation, userPrincipal.getId());
    }

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        User sender = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new NotFoundException("User", userPrincipal.getId().toString()));

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new NotFoundException("Conversation", request.getConversationId().toString()));

        boolean isClient = conversation.getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = conversation.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this conversation", "ACCESS_DENIED");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setText(request.getText());
        message.setIsRead(false);

        message = messageRepository.save(message);
        return mapMessageToResponse(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessages(Long conversationId, Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation", conversationId.toString()));

        boolean isClient = conversation.getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = conversation.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this conversation", "ACCESS_DENIED");
        }

        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);

        messageRepository.markMessagesAsRead(conversationId, userPrincipal.getId());

        return messages.map(this::mapMessageToResponse);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations() {
        UserPrincipal userPrincipal = getCurrentUser();
        List<Conversation> conversations = conversationRepository.findByUserId(userPrincipal.getId());

        return conversations.stream()
                .map(c -> mapConversationToResponse(c, userPrincipal.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(Long conversationId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation", conversationId.toString()));

        boolean isClient = conversation.getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = conversation.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this conversation", "ACCESS_DENIED");
        }

        return mapConversationToResponse(conversation, userPrincipal.getId());
    }


    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private ConversationResponse mapConversationToResponse(Conversation conversation, Long currentUserId) {
        ConversationResponse response = conversationMapper.toResponse(conversation);
        Long unreadCount = messageRepository.countUnreadMessages(conversation.getId(), currentUserId);
        response.setUnreadCount(unreadCount);
        return response;
    }

    private MessageResponse mapMessageToResponse(Message message) {
        return messageMapper.toResponse(message);
    }
}

