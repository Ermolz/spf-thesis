package com.example.freelance.mapper.review;

import com.example.freelance.domain.review.Review;
import com.example.freelance.dto.review.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorEmail", source = "author.email")
    @Mapping(target = "targetFreelancerId", source = "targetFreelancer.id", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "targetFreelancerDisplayName", source = "targetFreelancer.displayName", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "targetClientId", source = "targetClient.id", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "targetClientEmail", source = "targetClient.user.email", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "assignmentProjectTitle", source = "assignment.project.title")
    ReviewResponse toResponse(Review review);
}

