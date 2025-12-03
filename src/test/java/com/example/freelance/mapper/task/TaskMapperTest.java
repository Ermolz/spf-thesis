package com.example.freelance.mapper.task;

import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.task.Task;
import com.example.freelance.domain.task.TaskStatus;
import com.example.freelance.dto.task.TaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TaskMapperImpl.class, TaskAttachmentMapperImpl.class})
class TaskMapperTest {

    @Autowired
    private TaskMapper taskMapper;

    @Test
    void toResponse_WhenTaskIsValid_ShouldReturnResponse() {
        Assignment assignment = new Assignment();
        assignment.setId(50L);

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Fix Bug");
        task.setDescription("Fix critical bug");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setDeadline(Instant.parse("2024-12-31T00:00:00Z"));
        task.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        task.setAssignment(assignment);

        TaskResponse response = taskMapper.toResponse(task);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getAssignmentId()).isEqualTo(50L);
        assertThat(response.getTitle()).isEqualTo("Fix Bug");
        assertThat(response.getDescription()).isEqualTo("Fix critical bug");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.getDeadline()).isEqualTo("2024-12-31T00:00:00Z");
    }

    @Test
    void toResponseList_WhenTaskListProvided_ShouldReturnListOfResponses() {
        Assignment assignment = new Assignment();
        assignment.setId(50L);

        Task task1 = new Task();
        task1.setId(1L);
        task1.setAssignment(assignment);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setAssignment(assignment);

        List<TaskResponse> responses = taskMapper.toResponseList(List.of(task1, task2));

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
    }
}