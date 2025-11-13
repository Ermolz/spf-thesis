package com.example.freelance.repository.task;

import com.example.freelance.domain.task.Task;
import com.example.freelance.domain.task.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    Page<Task> findByAssignmentId(Long assignmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    Page<Task> findByAssignmentIdAndStatus(Long assignmentId, TaskStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    @Query("SELECT t FROM Task t WHERE t.assignment.id = :assignmentId")
    List<Task> findAllByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignment.id = :assignmentId AND t.status = :status")
    long countByAssignmentIdAndStatus(@Param("assignmentId") Long assignmentId, @Param("status") TaskStatus status);

    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    Optional<Task> findByIdAndAssignmentId(Long id, Long assignmentId);
    
    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    @Override
    Optional<Task> findById(Long id);
}

