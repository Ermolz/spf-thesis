package com.example.freelance.mapper.chat;

import com.example.freelance.domain.chat.Message;
import com.example.freelance.dto.chat.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderEmail", source = "sender.email")
    MessageResponse toResponse(Message message);
}

