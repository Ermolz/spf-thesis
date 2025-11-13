package com.example.freelance.repository.proposal;

import com.example.freelance.domain.proposal.Proposal;
import com.example.freelance.domain.proposal.ProposalStatus;
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
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    @EntityGraph(attributePaths = {"project.client.user", "freelancer.user"})
    Page<Proposal> findByFreelancerId(Long freelancerId, Pageable pageable);

    @EntityGraph(attributePaths = {"project.client.user", "freelancer.user"})
    Page<Proposal> findByProjectId(Long projectId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"project.client.user", "freelancer.user"})
    Optional<Proposal> findById(Long id);

    Page<Proposal> findByProjectIdAndStatus(Long projectId, ProposalStatus status, Pageable pageable);

    Optional<Proposal> findByProjectIdAndFreelancerId(Long projectId, Long freelancerId);

    boolean existsByProjectIdAndFreelancerId(Long projectId, Long freelancerId);

    @Query("SELECT p FROM Proposal p WHERE p.project.id = :projectId AND p.status = :status")
    List<Proposal> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") ProposalStatus status);

    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.project.id = :projectId AND p.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") ProposalStatus status);
}

