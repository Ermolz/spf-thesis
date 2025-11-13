package com.example.freelance.mapper.payment;

import com.example.freelance.domain.payment.Payment;
import com.example.freelance.dto.payment.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "assignmentId", source = "assignment.id")
    PaymentResponse toResponse(Payment payment);
}

