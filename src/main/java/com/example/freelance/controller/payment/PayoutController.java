package com.example.freelance.controller.payment;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.payment.CreatePayoutRequest;
import com.example.freelance.dto.payment.PayoutResponse;
import com.example.freelance.service.payment.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Payouts", description = "Payout management endpoints. Freelancers can request payouts of their earned funds.")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
public class PayoutController {
    private final PayoutService payoutService;

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a payout request",
            description = """
                    Creates a payout request for the authenticated freelancer.
                    
                    **Payout Process:**
                    1. Validates available balance is sufficient
                    2. Creates payout request with PENDING status
                    3. Deducts amount from available balance
                    4. Payout is processed asynchronously
                    
                    **Payout Methods:**
                    - BANK_TRANSFER: Direct bank transfer
                    - PAYPAL: PayPal account
                    - STRIPE: Stripe account
                    - CRYPTO: Cryptocurrency wallet
                    
                    **Payout Status:**
                    - PENDING: Awaiting processing
                    - PROCESSING: Being processed
                    - COMPLETED: Successfully processed
                    - FAILED: Processing failed
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payout request details",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreatePayoutRequest.class)
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payout request successfully created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Insufficient balance or invalid amount"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only freelancers can create payouts")
    })
    @PostMapping
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<PayoutResponse>> createPayout(@Valid @RequestBody CreatePayoutRequest request) {
        PayoutResponse response = payoutService.createPayout(request);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get payout by ID",
            description = "Retrieves a single payout by its ID. Only the payout owner can view it.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "Payout unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payout found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the payout owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payout not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<PayoutResponse>> getPayoutById(@PathVariable Long id) {
        PayoutResponse response = payoutService.getPayoutById(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get freelancer's payouts",
            description = """
                    Retrieves paginated list of payout requests for the authenticated freelancer.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Page size", example = "20"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payouts retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only freelancers can view their payouts")
    })
    @GetMapping("/my")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getMyPayouts(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<PayoutResponse> response = payoutService.getMyPayouts(pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get available balance",
            description = """
                    Retrieves the available balance for the authenticated freelancer.
                    
                    **Balance Calculation:**
                    - Sum of all completed RELEASE payments
                    - Minus sum of all completed payouts
                    - Represents funds available for withdrawal
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Balance retrieved successfully",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class),
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Balance Response",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": 1500.00,
                                                      "metadata": {
                                                        "timestamp": "2024-01-15T10:30:00Z"
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only freelancers can view their balance"
                    )
            }
    )
    @GetMapping("/balance")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<BigDecimal>> getAvailableBalance() {
        BigDecimal balance = payoutService.getAvailableBalance();
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(balance));
    }
}

