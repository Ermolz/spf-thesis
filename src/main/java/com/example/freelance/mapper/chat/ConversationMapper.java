package com.example.freelance.mapper.chat;

import com.example.freelance.domain.chat.Conversation;
import com.example.freelance.dto.chat.ConversationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "projectId", source = "project.id", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "projectTitle", source = "project.title", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "assignmentId", source = "assignment.id", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientEmail", source = "client.user.email")
    @Mapping(target = "freelancerId", source = "freelancer.id")
    @Mapping(target = "freelancerEmail", source = "freelancer.user.email")
    @Mapping(target = "freelancerDisplayName", source = "freelancer.displayName")
    @Mapping(target = "unreadCount", ignore = true)
    ConversationResponse toResponse(Conversation conversation);
}

