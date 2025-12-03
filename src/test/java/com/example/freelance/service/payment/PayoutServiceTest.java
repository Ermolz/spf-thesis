package com.example.freelance.service.payment;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.domain.payment.Payout;
import com.example.freelance.domain.payment.PayoutMethod;
import com.example.freelance.domain.payment.PayoutStatus;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.dto.payment.CreatePayoutRequest;
import com.example.freelance.dto.payment.PayoutResponse;
import com.example.freelance.mapper.payment.PayoutMapper;
import com.example.freelance.repository.payment.PaymentRepository;
import com.example.freelance.repository.payment.PayoutRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutServiceTest {

    @Mock private PayoutRepository payoutRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private FreelancerProfileRepository freelancerProfileRepository;
    @Mock private PayoutMapper payoutMapper;

    private PayoutService payoutService;
    private static final Long AUTH_USER_ID = 10L;

    @BeforeEach
    void setUp() {
        payoutService = new PayoutService(
                payoutRepository,
                paymentRepository,
                freelancerProfileRepository,
                payoutMapper
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
    void createPayout_WhenBalanceSufficient_ShouldCreatePayout() {
        CreatePayoutRequest request = new CreatePayoutRequest(new BigDecimal("100.00"), "USD", PayoutMethod.PAYPAL, "email@test.com", "Desc");

        when(paymentRepository.sumByFreelancerId(AUTH_USER_ID)).thenReturn(new BigDecimal("500.00"));
        when(payoutRepository.sumByUserIdAndStatus(AUTH_USER_ID, PayoutStatus.COMPLETED)).thenReturn(new BigDecimal("200.00"));
        when(payoutRepository.sumByUserIdAndStatus(AUTH_USER_ID, PayoutStatus.PENDING)).thenReturn(BigDecimal.ZERO);

        FreelancerProfile profile = new FreelancerProfile();
        when(freelancerProfileRepository.findByUserId(AUTH_USER_ID)).thenReturn(Optional.of(profile));

        when(payoutRepository.save(any(Payout.class))).thenAnswer(i -> i.getArgument(0));
        when(payoutMapper.toResponse(any(Payout.class))).thenReturn(PayoutResponse.builder().status(PayoutStatus.COMPLETED).amount(request.getAmount()).build());

        PayoutResponse response = payoutService.createPayout(request);

        assertThat(response.getStatus()).isEqualTo(PayoutStatus.COMPLETED);
        verify(payoutRepository, times(3)).save(any(Payout.class));
    }

    @Test
    void createPayout_WhenBalanceInsufficient_ShouldThrowBadRequest() {
        CreatePayoutRequest request = new CreatePayoutRequest(new BigDecimal("1000.00"), "USD", PayoutMethod.PAYPAL, null, null);

        when(paymentRepository.sumByFreelancerId(AUTH_USER_ID)).thenReturn(new BigDecimal("500.00"));
        when(payoutRepository.sumByUserIdAndStatus(AUTH_USER_ID, PayoutStatus.COMPLETED)).thenReturn(BigDecimal.ZERO);
        when(payoutRepository.sumByUserIdAndStatus(AUTH_USER_ID, PayoutStatus.PENDING)).thenReturn(BigDecimal.ZERO);

        assertThrows(BadRequestException.class, () -> payoutService.createPayout(request));
        verify(payoutRepository, never()).save(any());
    }
}