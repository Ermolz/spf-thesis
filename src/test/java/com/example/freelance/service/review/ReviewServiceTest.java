package com.example.freelance.service.review;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.assignment.AssignmentStatus;
import com.example.freelance.domain.project.Project;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private FreelancerProfileRepository freelancerProfileRepository;
    @Mock private ClientProfileRepository clientProfileRepository;
    @Mock private ReviewMapper reviewMapper;

    private ReviewService reviewService;

    private static final Long AUTH_USER_ID = 10L;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(
                reviewRepository,
                assignmentRepository,
                userRepository,
                freelancerProfileRepository,
                clientProfileRepository,
                reviewMapper
        );

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(AUTH_USER_ID);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReview_WhenAssignmentCompleted_ShouldCalculateRating() {
        CreateReviewRequest request = new CreateReviewRequest(1L, new BigDecimal("5.0"), "Good", ReviewType.CLIENT_TO_FREELANCER);

        User clientUser = new User(); clientUser.setId(AUTH_USER_ID);
        ClientProfile clientProfile = new ClientProfile(); clientProfile.setUser(clientUser);
        Project project = new Project(); project.setClient(clientProfile);

        User freelancerUser = new User(); freelancerUser.setId(20L);
        FreelancerProfile freelancerProfile = new FreelancerProfile();
        freelancerProfile.setId(2L);
        freelancerProfile.setUser(freelancerUser);

        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setProject(project);
        assignment.setFreelancer(freelancerProfile);

        when(userRepository.findById(AUTH_USER_ID)).thenReturn(Optional.of(clientUser));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(reviewRepository.findByAssignmentIdAndAuthorIdAndReviewType(any(), any(), any())).thenReturn(Optional.empty());

        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = i.getArgument(0);
            r.setTargetFreelancer(freelancerProfile);
            return r;
        });

        when(reviewRepository.getAverageRatingByFreelancerId(2L)).thenReturn(new BigDecimal("4.5"));
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(new ReviewResponse());

        reviewService.createReview(request);

        verify(freelancerProfileRepository).save(argThat(profile ->
                profile.getRating().equals(new BigDecimal("4.50"))
        ));
    }

    @Test
    void createReview_WhenAssignmentActive_ShouldThrowBadRequest() {
        CreateReviewRequest request = new CreateReviewRequest(1L, new BigDecimal("5.0"), "Good", ReviewType.CLIENT_TO_FREELANCER);
        Assignment assignment = new Assignment();
        assignment.setStatus(AssignmentStatus.ACTIVE);

        when(userRepository.findById(AUTH_USER_ID)).thenReturn(Optional.of(new User()));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(BadRequestException.class, () -> reviewService.createReview(request));
    }
}