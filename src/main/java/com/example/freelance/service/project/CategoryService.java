package com.example.freelance.service.project;

import com.example.freelance.domain.project.Category;
import com.example.freelance.dto.project.CategoryResponse;
import com.example.freelance.mapper.project.CategoryMapper;
import com.example.freelance.repository.project.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toResponseList(categories);
    }
}

