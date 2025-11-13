package com.example.freelance.mapper.task;

import com.example.freelance.domain.task.Task;
import com.example.freelance.domain.task.TaskAttachment;
import com.example.freelance.dto.task.TaskAttachmentResponse;
import com.example.freelance.dto.task.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = TaskAttachmentMapper.class)
public interface TaskMapper {

    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "attachments", ignore = true)
    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);
}

