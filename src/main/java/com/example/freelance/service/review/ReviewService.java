package com.example.freelance.service.review;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ConflictException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.assignment.AssignmentStatus;
import com.example.freelance.domain.review.Review;
import com.example.freelance.domain.review.ReviewType;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.review.CreateReviewRequest;
import com.example.freelance.dto.review.ReviewResponse;
import com.example.freelance.mapper.review.ReviewMapper;
import com.example.freelance.repository.assignment.AssignmentRepository;
import com.example.freelance.repository.review.ReviewRepository;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.repository.user.UserRepository;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        User author = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new NotFoundException("User", userPrincipal.getId().toString()));

        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new NotFoundException("Assignment", request.getAssignmentId().toString()));

        if (assignment.getStatus() != AssignmentStatus.COMPLETED) {
            throw new BadRequestException("Can only create reviews for completed assignments", "ASSIGNMENT_NOT_COMPLETED");
        }

        boolean isClient = assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = assignment.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this assignment", "ACCESS_DENIED");
        }

        if (request.getReviewType() == ReviewType.CLIENT_TO_FREELANCER && !isClient) {
            throw new ForbiddenException("Only client can create reviews for freelancer", "NOT_CLIENT");
        }

        if (request.getReviewType() == ReviewType.FREELANCER_TO_CLIENT && !isFreelancer) {
            throw new ForbiddenException("Only freelancer can create reviews for client", "NOT_FREELANCER");
        }

        if (reviewRepository.findByAssignmentIdAndAuthorIdAndReviewType(
                assignment.getId(), userPrincipal.getId(), request.getReviewType()).isPresent()) {
            throw new ConflictException("Review already exists for this assignment and type", "REVIEW_ALREADY_EXISTS");
        }

        Review review = new Review();
        review.setAuthor(author);
        review.setAssignment(assignment);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setReviewType(request.getReviewType());

        if (request.getReviewType() == ReviewType.CLIENT_TO_FREELANCER) {
            review.setTargetFreelancer(assignment.getFreelancer());
        } else {
            review.setTargetClient(assignment.getProject().getClient());
        }

        review = reviewRepository.save(review);
        updateProfileRating(review);

        return mapToResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getFreelancerReviews(Long freelancerId, Pageable pageable) {
        FreelancerProfile freelancer = freelancerProfileRepository.findById(freelancerId)
                .orElseThrow(() -> new NotFoundException("FreelancerProfile", freelancerId.toString()));

        Page<Review> reviews = reviewRepository.findByTargetFreelancerId(freelancerId, pageable);
        return reviews.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getClientReviews(Long clientId, Pageable pageable) {
        ClientProfile client = clientProfileRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("ClientProfile", clientId.toString()));

        Page<Review> reviews = reviewRepository.findByTargetClientId(clientId, pageable);
        return reviews.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAssignmentReviews(Long assignmentId, Pageable pageable) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment", assignmentId.toString()));

        UserPrincipal userPrincipal = getCurrentUser();
        boolean isClient = assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = assignment.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this assignment", "ACCESS_DENIED");
        }

        List<Review> allReviews = reviewRepository.findByAssignmentId(assignmentId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allReviews.size());
        List<Review> pageContent = allReviews.subList(start, end);
        Page<Review> reviews = new PageImpl<>(pageContent, pageable, allReviews.size());

        return reviews.map(this::mapToResponse);
    }

    private void updateProfileRating(Review review) {
        if (review.getReviewType() == ReviewType.CLIENT_TO_FREELANCER && review.getTargetFreelancer() != null) {
            BigDecimal averageRating = reviewRepository.getAverageRatingByFreelancerId(review.getTargetFreelancer().getId());
            if (averageRating != null) {
                FreelancerProfile profile = review.getTargetFreelancer();
                profile.setRating(averageRating.setScale(2, RoundingMode.HALF_UP));
                freelancerProfileRepository.save(profile);
            }
        } else if (review.getReviewType() == ReviewType.FREELANCER_TO_CLIENT && review.getTargetClient() != null) {
            BigDecimal averageRating = reviewRepository.getAverageRatingByClientId(review.getTargetClient().getId());
            if (averageRating != null) {
                ClientProfile profile = review.getTargetClient();
                profile.setRating(averageRating.setScale(2, RoundingMode.HALF_UP));
                clientProfileRepository.save(profile);
            }
        }
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private ReviewResponse mapToResponse(Review review) {
        return reviewMapper.toResponse(review);
    }
}

