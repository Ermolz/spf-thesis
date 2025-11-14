package com.example.freelance.mapper.project;

import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.project.Tag;
import com.example.freelance.dto.project.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientEmail", source = "client.user.email")
    @Mapping(target = "categoryId", source = "category.id", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "categoryName", source = "category.name", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "tagNames", source = "tags", qualifiedByName = "tagsToNames")
    ProjectResponse toResponse(Project project);

    @Named("tagsToNames")
    default List<String> tagsToNames(List<Tag> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .map(Tag::getName)
                .toList();
    }
}

