package com.example.freelance.mapper.proposal;

import com.example.freelance.domain.proposal.Proposal;
import com.example.freelance.dto.proposal.ProposalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProposalMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectTitle", source = "project.title")
    @Mapping(target = "freelancerId", source = "freelancer.id")
    @Mapping(target = "freelancerEmail", source = "freelancer.user.email")
    @Mapping(target = "freelancerDisplayName", source = "freelancer.displayName")
    ProposalResponse toResponse(Proposal proposal);
}

