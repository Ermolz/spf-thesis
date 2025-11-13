package com.example.freelance.mapper.user;

import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.dto.user.FreelancerProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FreelancerProfileMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    FreelancerProfileResponse toResponse(FreelancerProfile profile);
}

