package com.example.freelance.repository.task;

import com.example.freelance.domain.task.TaskAttachment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findByTaskId(Long taskId);

    @EntityGraph(attributePaths = {"task.assignment.project.client.user", "task.assignment.freelancer.user"})
    @Override
    Optional<TaskAttachment> findById(Long id);

    Optional<TaskAttachment> findByIdAndTaskId(Long id, Long taskId);

    void deleteByTaskId(Long taskId);
}

