package com.example.freelance.dto.payment;

import com.example.freelance.domain.payment.PaymentStatus;
import com.example.freelance.domain.payment.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long assignmentId;
    private BigDecimal amount;
    private String currency;
    private PaymentType type;
    private PaymentStatus status;
    private String transactionId;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}

