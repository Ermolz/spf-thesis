package com.example.freelance.service.payment;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.assignment.AssignmentStatus;
import com.example.freelance.domain.payment.Payment;
import com.example.freelance.domain.payment.PaymentStatus;
import com.example.freelance.domain.payment.PaymentType;
import com.example.freelance.dto.payment.CreatePaymentRequest;
import com.example.freelance.dto.payment.PaymentResponse;
import com.example.freelance.mapper.payment.PaymentMapper;
import com.example.freelance.repository.assignment.AssignmentRepository;
import com.example.freelance.repository.payment.PaymentRepository;
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
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AssignmentRepository assignmentRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("CREATE_PAYMENT");
        
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new NotFoundException("Assignment", request.getAssignmentId().toString()));
        
        log.info("Creating payment: assignmentId={}, type={}, amount={}, currency={}", 
                request.getAssignmentId(), request.getType(), request.getAmount(), request.getCurrency());

        if (!assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId())) {
            throw new ForbiddenException("Only project owner can create payments", "NOT_PROJECT_OWNER");
        }

        if (assignment.getStatus() != AssignmentStatus.ACTIVE && assignment.getStatus() != AssignmentStatus.COMPLETED) {
            throw new BadRequestException("Can only create payments for active or completed assignments", "INVALID_ASSIGNMENT_STATUS");
        }

        if (request.getType() == PaymentType.RELEASE) {
            BigDecimal totalEscrow = BigDecimal.ZERO;
            BigDecimal totalReleased = BigDecimal.ZERO;

            for (Payment payment : paymentRepository.findByAssignmentId(assignment.getId(), Pageable.unpaged()).getContent()) {
                if (payment.getStatus() == PaymentStatus.COMPLETED) {
                    if (payment.getType() == PaymentType.ESCROW) {
                        totalEscrow = totalEscrow.add(payment.getAmount());
                    } else if (payment.getType() == PaymentType.RELEASE) {
                        totalReleased = totalReleased.add(payment.getAmount());
                    }
                }
            }

            if (totalReleased.add(request.getAmount()).compareTo(totalEscrow) > 0) {
                throw new BadRequestException("Cannot release more than available in escrow", "INSUFFICIENT_ESCROW");
            }
        }

        Payment payment = new Payment();
        payment.setAssignment(assignment);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency().toUpperCase());
        payment.setType(request.getType());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setDescription(request.getDescription());

        payment = paymentRepository.save(payment);

        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment = paymentRepository.save(payment);
        
        log.info("Payment created successfully: paymentId={}, transactionId={}, status={}, amount={}", 
                payment.getId(), payment.getTransactionId(), payment.getStatus(), payment.getAmount());
        MdcUtil.clearCustomValues();

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment", paymentId.toString()));

        UserPrincipal userPrincipal = getCurrentUser();
        boolean isClient = payment.getAssignment().getProject().getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = payment.getAssignment().getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this payment", "ACCESS_DENIED");
        }

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByAssignment(Long assignmentId, Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment", assignmentId.toString()));

        boolean isClient = assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = assignment.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this assignment", "ACCESS_DENIED");
        }

        Page<Payment> payments = paymentRepository.findByAssignmentId(assignmentId, pageable);
        return payments.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        Page<Payment> payments = paymentRepository.findByFreelancerId(userPrincipal.getId(), pageable);
        return payments.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getClientPayments(Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        Page<Payment> payments = paymentRepository.findByClientId(userPrincipal.getId(), pageable);
        return payments.map(this::mapToResponse);
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return paymentMapper.toResponse(payment);
    }
}

