package com.example.freelance.repository.payment;

import com.example.freelance.domain.payment.Payout;
import com.example.freelance.domain.payment.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {
    Page<Payout> findByFreelancerId(Long freelancerId, Pageable pageable);

    Page<Payout> findByFreelancerIdAndStatus(Long freelancerId, PayoutStatus status, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM Payout p WHERE p.freelancer.id = :freelancerId AND p.status = :status")
    BigDecimal sumByFreelancerIdAndStatus(@Param("freelancerId") Long freelancerId, @Param("status") PayoutStatus status);

    @Query("SELECT SUM(p.amount) FROM Payout p WHERE p.freelancer.user.id = :userId AND p.status = :status")
    BigDecimal sumByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PayoutStatus status);

    List<Payout> findByStatus(PayoutStatus status);
}

