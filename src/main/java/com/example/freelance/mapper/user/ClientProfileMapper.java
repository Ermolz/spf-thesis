package com.example.freelance.mapper.user;

import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.dto.user.ClientProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientProfileMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    ClientProfileResponse toResponse(ClientProfile profile);
}

