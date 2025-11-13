package com.example.freelance.repository.task;

import com.example.freelance.domain.task.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findByTaskId(Long taskId);

    Optional<TaskAttachment> findByIdAndTaskId(Long id, Long taskId);

    void deleteByTaskId(Long taskId);
}

