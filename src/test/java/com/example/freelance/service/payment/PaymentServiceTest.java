package com.example.freelance.service.payment;

import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.assignment.AssignmentStatus;
import com.example.freelance.domain.payment.Payment;
import com.example.freelance.domain.payment.PaymentStatus;
import com.example.freelance.domain.payment.PaymentType;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.payment.CreatePaymentRequest;
import com.example.freelance.dto.payment.PaymentResponse;
import com.example.freelance.mapper.payment.PaymentMapper;
import com.example.freelance.repository.assignment.AssignmentRepository;
import com.example.freelance.repository.payment.PaymentRepository;
import com.example.freelance.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private static final Long CLIENT_ID = 10L;

    @BeforeEach
    void setUp() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserPrincipal principal = mock(UserPrincipal.class);

        when(principal.getId()).thenReturn(CLIENT_ID);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPayment_WhenValidEscrow_ShouldSucceed() {
        CreatePaymentRequest request = new CreatePaymentRequest(100L, new BigDecimal("500.00"), "USD", PaymentType.ESCROW, "Advance");

        User clientUser = new User(); clientUser.setId(CLIENT_ID);
        ClientProfile clientProfile = new ClientProfile(); clientProfile.setUser(clientUser);
        Project project = new Project(); project.setClient(clientProfile);

        Assignment assignment = new Assignment();
        assignment.setId(100L);
        assignment.setProject(project);
        assignment.setStatus(AssignmentStatus.ACTIVE);

        when(assignmentRepository.findById(request.getAssignmentId())).thenReturn(Optional.of(assignment));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentResponse expectedResponse = PaymentResponse.builder().status(PaymentStatus.COMPLETED).amount(request.getAmount()).build();
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(expectedResponse);

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository, times(3)).save(any(Payment.class));
    }

    @Test
    void createPayment_WhenNotProjectOwner_ShouldThrowForbidden() {
        CreatePaymentRequest request = new CreatePaymentRequest(100L, new BigDecimal("100.00"), "USD", PaymentType.ESCROW, "Scam");

        User otherUser = new User(); otherUser.setId(999L);
        ClientProfile clientProfile = new ClientProfile(); clientProfile.setUser(otherUser);
        Project project = new Project(); project.setClient(clientProfile);

        Assignment assignment = new Assignment();
        assignment.setProject(project);

        when(assignmentRepository.findById(request.getAssignmentId())).thenReturn(Optional.of(assignment));

        assertThrows(ForbiddenException.class, () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any());
    }
}