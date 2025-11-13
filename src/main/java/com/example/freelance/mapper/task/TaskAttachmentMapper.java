package com.example.freelance.mapper.task;

import com.example.freelance.domain.task.TaskAttachment;
import com.example.freelance.dto.task.TaskAttachmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskAttachmentMapper {

    @Mapping(target = "uploadedById", source = "uploadedBy.id")
    @Mapping(target = "uploadedByEmail", source = "uploadedBy.email")
    TaskAttachmentResponse toResponse(TaskAttachment attachment);
}

