package com.example.freelance.repository.assignment;

import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.assignment.AssignmentStatus;
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
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findByProjectId(Long projectId);

    Optional<Assignment> findByProposalId(Long proposalId);

    Page<Assignment> findByFreelancerIdAndStatus(Long freelancerId, AssignmentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"project.client.user", "freelancer.user", "proposal"})
    @Query("SELECT a FROM Assignment a WHERE a.project.client.user.id = :clientId")
    Page<Assignment> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"project.client.user", "freelancer.user", "proposal"})
    @Query("SELECT a FROM Assignment a WHERE a.project.client.user.id = :clientId AND a.status = :status")
    Page<Assignment> findByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") AssignmentStatus status, Pageable pageable);
    
    @EntityGraph(attributePaths = {"project.client.user", "freelancer.user", "proposal"})
    Page<Assignment> findByFreelancerId(Long freelancerId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"project.client.user", "freelancer.user", "proposal"})
    Optional<Assignment> findById(Long id);

    List<Assignment> findByStatus(AssignmentStatus status);

    boolean existsByProjectId(Long projectId);
}

