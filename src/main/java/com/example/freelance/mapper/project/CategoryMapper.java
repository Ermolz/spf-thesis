package com.example.freelance.mapper.project;

import com.example.freelance.domain.project.Category;
import com.example.freelance.dto.project.CategoryResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
    List<CategoryResponse> toResponseList(List<Category> categories);
}

