package com.example.freelance.service.payment;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.payment.Payout;
import com.example.freelance.domain.payment.PayoutStatus;
import com.example.freelance.dto.payment.CreatePayoutRequest;
import com.example.freelance.dto.payment.PayoutResponse;
import com.example.freelance.mapper.payment.PayoutMapper;
import com.example.freelance.repository.payment.PaymentRepository;
import com.example.freelance.repository.payment.PayoutRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.common.util.MdcUtil;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {
    private final PayoutRepository payoutRepository;
    private final PaymentRepository paymentRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final PayoutMapper payoutMapper;

    @Transactional
    public PayoutResponse createPayout(CreatePayoutRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("CREATE_PAYOUT");
        
        var freelancer = freelancerProfileRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Freelancer profile not found", "FREELANCER_PROFILE_NOT_FOUND"));
        
        log.info("Creating payout: freelancerId={}, amount={}, currency={}, method={}", 
                userPrincipal.getId(), request.getAmount(), request.getCurrency(), request.getPayoutMethod());

        BigDecimal totalEarned = paymentRepository.sumByFreelancerId(userPrincipal.getId());
        if (totalEarned == null) {
            totalEarned = BigDecimal.ZERO;
        }

        BigDecimal totalPayouts = payoutRepository.sumByUserIdAndStatus(userPrincipal.getId(), PayoutStatus.COMPLETED);
        if (totalPayouts == null) {
            totalPayouts = BigDecimal.ZERO;
        }

        BigDecimal pendingPayouts = payoutRepository.sumByUserIdAndStatus(userPrincipal.getId(), PayoutStatus.PENDING);
        if (pendingPayouts == null) {
            pendingPayouts = BigDecimal.ZERO;
        }

        BigDecimal availableBalance = totalEarned.subtract(totalPayouts).subtract(pendingPayouts);

        if (request.getAmount().compareTo(availableBalance) > 0) {
            throw new BadRequestException(
                    String.format("Insufficient balance. Available: %s, Requested: %s", availableBalance, request.getAmount()),
                    "INSUFFICIENT_BALANCE"
            );
        }

        Payout payout = new Payout();
        payout.setFreelancer(freelancer);
        payout.setAmount(request.getAmount());
        payout.setCurrency(request.getCurrency().toUpperCase());
        payout.setPayoutMethod(request.getPayoutMethod());
        payout.setStatus(PayoutStatus.PENDING);
        payout.setAccountDetails(request.getAccountDetails());
        payout.setTransactionId(UUID.randomUUID().toString());
        payout.setDescription(request.getDescription());

        payout = payoutRepository.save(payout);

        payout.setStatus(PayoutStatus.PROCESSING);
        payout = payoutRepository.save(payout);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        payout.setStatus(PayoutStatus.COMPLETED);
        payout = payoutRepository.save(payout);
        
        log.info("Payout created successfully: payoutId={}, transactionId={}, status={}, amount={}", 
                payout.getId(), payout.getTransactionId(), payout.getStatus(), payout.getAmount());
        MdcUtil.clearCustomValues();

        return mapToResponse(payout);
    }

    @Transactional(readOnly = true)
    public PayoutResponse getPayoutById(Long payoutId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new NotFoundException("Payout", payoutId.toString()));

        UserPrincipal userPrincipal = getCurrentUser();
        if (!payout.getFreelancer().getUser().getId().equals(userPrincipal.getId())) {
            throw new ForbiddenException("Access denied to this payout", "ACCESS_DENIED");
        }

        return mapToResponse(payout);
    }

    @Transactional(readOnly = true)
    public Page<PayoutResponse> getMyPayouts(Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        var freelancer = freelancerProfileRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Freelancer profile not found", "FREELANCER_PROFILE_NOT_FOUND"));

        Page<Payout> payouts = payoutRepository.findByFreelancerId(freelancer.getId(), pageable);
        return payouts.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAvailableBalance() {
        UserPrincipal userPrincipal = getCurrentUser();
        var freelancer = freelancerProfileRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Freelancer profile not found", "FREELANCER_PROFILE_NOT_FOUND"));

        BigDecimal totalEarned = paymentRepository.sumByFreelancerId(userPrincipal.getId());
        if (totalEarned == null) {
            totalEarned = BigDecimal.ZERO;
        }

        BigDecimal totalPayouts = payoutRepository.sumByUserIdAndStatus(userPrincipal.getId(), PayoutStatus.COMPLETED);
        if (totalPayouts == null) {
            totalPayouts = BigDecimal.ZERO;
        }

        BigDecimal pendingPayouts = payoutRepository.sumByUserIdAndStatus(userPrincipal.getId(), PayoutStatus.PENDING);
        if (pendingPayouts == null) {
            pendingPayouts = BigDecimal.ZERO;
        }

        return totalEarned.subtract(totalPayouts).subtract(pendingPayouts);
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private PayoutResponse mapToResponse(Payout payout) {
        return payoutMapper.toResponse(payout);
    }
}

