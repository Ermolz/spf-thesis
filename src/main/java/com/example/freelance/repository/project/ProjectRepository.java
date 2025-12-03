package com.example.freelance.repository.project;

import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @EntityGraph(attributePaths = {"client.user", "category", "tags"})
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"client.user", "category", "tags"})
    Page<Project> findByClientId(Long clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"client.user", "category", "tags"})
    Page<Project> findByClientIdAndStatus(Long clientId, ProjectStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"client.user", "category", "tags"})
    @Query("SELECT p FROM Project p WHERE p.status = :status " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND p.budgetMax >= COALESCE(:minBudget, p.budgetMax) " +
           "AND p.budgetMin <= COALESCE(:maxBudget, p.budgetMin) " +
           "AND (:tagIds IS NULL OR EXISTS (SELECT t FROM p.tags t WHERE t.id IN :tagIds)) " +
           "AND p.deadline >= COALESCE(:minDeadline, p.deadline) " +
           "AND p.deadline <= COALESCE(:maxDeadline, p.deadline)")
    Page<Project> searchProjects(
            @Param("status") ProjectStatus status,
            @Param("categoryId") Long categoryId,
            @Param("minBudget") BigDecimal minBudget,
            @Param("maxBudget") BigDecimal maxBudget,
            @Param("tagIds") List<Long> tagIds,
            @Param("minDeadline") Instant minDeadline,
            @Param("maxDeadline") Instant maxDeadline,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"client.user", "category", "tags"})
    Optional<Project> findByIdAndClientId(Long id, Long clientId);
    
    @EntityGraph(attributePaths = {"client.user", "category", "tags"})
    @Override
    Optional<Project> findById(Long id);
}

