package com.example.freelance.controller.project;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.project.CategoryResponse;
import com.example.freelance.service.project.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints. Get list of available project categories.")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "Get all categories",
            description = """
                    Retrieves a list of all available project categories.
                    Available to all authenticated users.
                    
                    **Use Cases:**
                    - Get categories for project creation dropdown
                    - Filter projects by category
                    - Display category information
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ResponseEntity.ok(ResponseUtil.success(response));
    }
}

