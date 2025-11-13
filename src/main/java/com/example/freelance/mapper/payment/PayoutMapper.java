package com.example.freelance.mapper.payment;

import com.example.freelance.domain.payment.Payout;
import com.example.freelance.dto.payment.PayoutResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayoutMapper {

    @Mapping(target = "freelancerId", source = "freelancer.id")
    @Mapping(target = "freelancerEmail", source = "freelancer.user.email")
    PayoutResponse toResponse(Payout payout);
}

