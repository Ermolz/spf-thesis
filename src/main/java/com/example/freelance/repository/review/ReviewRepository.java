package com.example.freelance.repository.review;

import com.example.freelance.domain.review.Review;
import com.example.freelance.domain.review.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByTargetFreelancerId(Long freelancerId, Pageable pageable);

    Page<Review> findByTargetClientId(Long clientId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetFreelancer.id = :freelancerId")
    BigDecimal getAverageRatingByFreelancerId(@Param("freelancerId") Long freelancerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetClient.id = :clientId")
    BigDecimal getAverageRatingByClientId(@Param("clientId") Long clientId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.targetFreelancer.id = :freelancerId")
    long countByFreelancerId(@Param("freelancerId") Long freelancerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.targetClient.id = :clientId")
    long countByClientId(@Param("clientId") Long clientId);

    Optional<Review> findByAssignmentIdAndAuthorIdAndReviewType(Long assignmentId, Long authorId, ReviewType reviewType);

    @Query("SELECT r FROM Review r WHERE r.assignment.id = :assignmentId")
    List<Review> findByAssignmentId(@Param("assignmentId") Long assignmentId);
}

