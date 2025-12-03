package com.example.freelance.controller.skill;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.domain.skill.Skill;
import com.example.freelance.repository.skill.SkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Skill management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class SkillController {
    private final SkillRepository skillRepository;

    @Operation(
            summary = "Get all skills",
            description = "Retrieves a list of all available skills ordered by name"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<Skill>>> getAllSkills() {
        List<Skill> skills = skillRepository.findAllByOrderByNameAsc();
        return ResponseEntity.ok(ResponseUtil.success(skills));
    }
}

