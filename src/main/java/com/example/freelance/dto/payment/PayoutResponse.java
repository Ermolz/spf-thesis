package com.example.freelance.dto.payment;

import com.example.freelance.domain.payment.PayoutMethod;
import com.example.freelance.domain.payment.PayoutStatus;
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
public class PayoutResponse {
    private Long id;
    private Long freelancerId;
    private String freelancerEmail;
    private BigDecimal amount;
    private String currency;
    private PayoutMethod payoutMethod;
    private PayoutStatus status;
    private String accountDetails;
    private String transactionId;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}

