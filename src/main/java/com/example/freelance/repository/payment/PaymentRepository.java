package com.example.freelance.repository.payment;

import com.example.freelance.domain.payment.Payment;
import com.example.freelance.domain.payment.PaymentStatus;
import com.example.freelance.domain.payment.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    Page<Payment> findByAssignmentId(Long assignmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    Page<Payment> findByAssignmentIdAndStatus(Long assignmentId, PaymentStatus status, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.assignment.id = :assignmentId AND p.status = :status")
    BigDecimal sumByAssignmentIdAndStatus(@Param("assignmentId") Long assignmentId, @Param("status") PaymentStatus status);

    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    @Query("SELECT p FROM Payment p WHERE p.assignment.project.client.user.id = :clientId")
    Page<Payment> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    @Query("SELECT p FROM Payment p WHERE p.assignment.freelancer.user.id = :freelancerId")
    Page<Payment> findByFreelancerId(@Param("freelancerId") Long freelancerId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"assignment.project.client.user", "assignment.freelancer.user"})
    Optional<Payment> findById(Long id);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.assignment.freelancer.user.id = :freelancerId AND p.status = 'COMPLETED'")
    BigDecimal sumByFreelancerId(@Param("freelancerId") Long freelancerId);

    List<Payment> findByAssignmentIdAndType(Long assignmentId, PaymentType type);
}

