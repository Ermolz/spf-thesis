package com.example.freelance.dto.payment;

import com.example.freelance.domain.payment.PayoutMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayoutRequest {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotNull(message = "Payout method is required")
    private PayoutMethod payoutMethod;

    @Size(max = 1000, message = "Account details must not exceed 1000 characters")
    private String accountDetails;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}

