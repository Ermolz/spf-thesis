package com.example.freelance.controller.payment;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.payment.CreatePaymentRequest;
import com.example.freelance.dto.payment.PaymentResponse;
import com.example.freelance.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Payments", description = "Payment management endpoints. Handles escrow payments, payment releases, and payment history. Clients can create payments, both clients and freelancers can view payment history.")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    private final PaymentService paymentService;

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a payment",
            description = """
                    Creates a payment for an assignment. Only CLIENT role can create payments.
                    
                    **Payment Types:**
                    - ESCROW: Funds held in escrow for the assignment
                    - RELEASE: Release funds from escrow to freelancer
                    
                    **Payment Status:**
                    - PENDING: Payment is being processed
                    - COMPLETED: Payment successfully processed
                    - FAILED: Payment processing failed
                    
                    **Escrow Rules:**
                    - Total released funds cannot exceed total escrow funds
                    - Payments are validated before processing
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment successfully created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Invalid payment amount or escrow limit exceeded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only clients can create payments"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get payment by ID",
            description = "Retrieves a single payment by its ID. Only the client or freelancer associated with the assignment can view it.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "Payment unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this payment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get payments by assignment",
            description = """
                    Retrieves paginated list of payments for a specific assignment.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "assignmentId", description = "Assignment unique identifier", required = true, example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Page size", example = "20"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPaymentsByAssignment(
            @PathVariable Long assignmentId,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentResponse> response = paymentService.getPaymentsByAssignment(assignmentId, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get freelancer's payments",
            description = """
                    Retrieves paginated list of payments received by the authenticated freelancer.
                    
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only freelancers can view their payments")
    })
    @GetMapping("/my")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getMyPayments(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentResponse> response = paymentService.getMyPayments(pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get client's payments",
            description = """
                    Retrieves paginated list of payments made by the authenticated client.
                    
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only clients can view their payments")
    })
    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getClientPayments(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentResponse> response = paymentService.getClientPayments(pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }
}

