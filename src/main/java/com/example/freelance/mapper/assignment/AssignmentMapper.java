package com.example.freelance.mapper.assignment;

import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.dto.assignment.AssignmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectTitle", source = "project.title")
    @Mapping(target = "freelancerId", source = "freelancer.id")
    @Mapping(target = "freelancerEmail", source = "freelancer.user.email")
    @Mapping(target = "freelancerDisplayName", source = "freelancer.displayName")
    @Mapping(target = "clientId", source = "project.client.id")
    @Mapping(target = "clientEmail", source = "project.client.user.email")
    @Mapping(target = "proposalId", source = "proposal.id")
    AssignmentResponse toResponse(Assignment assignment);
}

