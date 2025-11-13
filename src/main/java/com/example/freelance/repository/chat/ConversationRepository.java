package com.example.freelance.repository.chat;

import com.example.freelance.domain.chat.Conversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @EntityGraph(attributePaths = {"client.user", "freelancer.user", "project", "assignment"})
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.project.id = :projectId OR c.assignment.id = :assignmentId) AND " +
           "c.client.id = :clientId AND c.freelancer.id = :freelancerId")
    Optional<Conversation> findByProjectOrAssignmentAndParticipants(
            @Param("projectId") Long projectId,
            @Param("assignmentId") Long assignmentId,
            @Param("clientId") Long clientId,
            @Param("freelancerId") Long freelancerId
    );

    @EntityGraph(attributePaths = {"client.user", "freelancer.user", "project", "assignment"})
    @Query("SELECT c FROM Conversation c WHERE c.client.user.id = :userId OR c.freelancer.user.id = :userId")
    List<Conversation> findByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"client.user", "freelancer.user", "project", "assignment"})
    @Query("SELECT c FROM Conversation c WHERE c.project.id = :projectId")
    List<Conversation> findByProjectId(@Param("projectId") Long projectId);

    @EntityGraph(attributePaths = {"client.user", "freelancer.user", "project", "assignment"})
    @Query("SELECT c FROM Conversation c WHERE c.assignment.id = :assignmentId")
    Optional<Conversation> findByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    @EntityGraph(attributePaths = {"client.user", "freelancer.user", "project", "assignment"})
    @Override
    Optional<Conversation> findById(Long id);
}

