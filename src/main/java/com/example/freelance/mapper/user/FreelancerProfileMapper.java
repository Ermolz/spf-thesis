package com.example.freelance.mapper.user;

import com.example.freelance.domain.skill.Skill;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.dto.user.FreelancerProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FreelancerProfileMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "skills", source = "skills", qualifiedByName = "skillsToNames")
    FreelancerProfileResponse toResponse(FreelancerProfile profile);

    @Named("skillsToNames")
    default List<String> skillsToNames(List<Skill> skills) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream()
                .map(Skill::getName)
                .toList();
    }
}