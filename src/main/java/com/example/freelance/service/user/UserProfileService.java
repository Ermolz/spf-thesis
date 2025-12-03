package com.example.freelance.service.user;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.skill.Skill;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.dto.user.ClientProfileResponse;
import com.example.freelance.dto.user.FreelancerProfileResponse;
import com.example.freelance.dto.user.UpdateClientProfileRequest;
import com.example.freelance.dto.user.UpdateFreelancerProfileRequest;
import com.example.freelance.mapper.user.ClientProfileMapper;
import com.example.freelance.mapper.user.FreelancerProfileMapper;
import com.example.freelance.repository.skill.SkillRepository;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.repository.user.UserRepository;
import com.example.freelance.util.FileStorageUtil;
import com.example.freelance.util.FileValidationUtil;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private static final String FREELANCER_PROFILE_NOT_FOUND_MESSAGE = "Freelancer profile not found";
    private static final String FREELANCER_PROFILE_NOT_FOUND_CODE = "FREELANCER_PROFILE_NOT_FOUND";
    
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final FileStorageUtil fileStorageUtil;
    private final FileValidationUtil fileValidationUtil;
    private final FreelancerProfileMapper freelancerProfileMapper;
    private final ClientProfileMapper clientProfileMapper;

    @Transactional(readOnly = true)
    public FreelancerProfileResponse getFreelancerProfile(Long userId) {
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("FreelancerProfile", userId.toString()));

        return mapFreelancerToResponse(profile);
    }

    @Transactional(readOnly = true)
    public FreelancerProfileResponse getMyFreelancerProfile() {
        UserPrincipal userPrincipal = getCurrentUser();
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException(FREELANCER_PROFILE_NOT_FOUND_MESSAGE, FREELANCER_PROFILE_NOT_FOUND_CODE));

        return mapFreelancerToResponse(profile);
    }

    @Transactional
    public FreelancerProfileResponse updateFreelancerProfile(UpdateFreelancerProfileRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException(FREELANCER_PROFILE_NOT_FOUND_MESSAGE, FREELANCER_PROFILE_NOT_FOUND_CODE));

        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getSkillIds() != null) {
            List<Skill> skills = skillRepository.findAllById(request.getSkillIds());
            if (skills.size() != request.getSkillIds().size()) {
                throw new BadRequestException("One or more skills not found", "SKILLS_NOT_FOUND");
            }
            profile.setSkills(skills);
        }
        if (request.getHourlyRate() != null) {
            profile.setHourlyRate(request.getHourlyRate());
        }
        if (request.getCurrency() != null) {
            profile.setCurrency(request.getCurrency().toUpperCase());
        }

        profile = freelancerProfileRepository.save(profile);
        return mapFreelancerToResponse(profile);
    }

    @Transactional
    public FreelancerProfileResponse uploadPortfolio(MultipartFile file) {
        UserPrincipal userPrincipal = getCurrentUser();
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException(FREELANCER_PROFILE_NOT_FOUND_MESSAGE, FREELANCER_PROFILE_NOT_FOUND_CODE));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty", "EMPTY_FILE");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are allowed for portfolio", "INVALID_FILE_TYPE");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new BadRequestException("Only PDF files are allowed for portfolio", "INVALID_FILE_TYPE");
        }

        long maxSize = 10485760; // 10MB
        if (file.getSize() > maxSize) {
            throw new BadRequestException("File size exceeds 10MB limit", "FILE_TOO_LARGE");
        }

        try {
            if (profile.getPortfolioFilePath() != null) {
                fileStorageUtil.deleteFile(profile.getPortfolioFilePath());
            }

            String filePath = fileStorageUtil.storeFile(file, "portfolios/" + profile.getId());
            profile.setPortfolioFilePath(filePath);
            profile = freelancerProfileRepository.save(profile);

            return mapFreelancerToResponse(profile);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload portfolio", "FILE_UPLOAD_FAILED");
        }
    }

    @Transactional(readOnly = true)
    public ClientProfileResponse getClientProfile(Long userId) {
        ClientProfile profile = clientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("ClientProfile", userId.toString()));

        return mapClientToResponse(profile);
    }

    @Transactional(readOnly = true)
    public ClientProfileResponse getMyClientProfile() {
        UserPrincipal userPrincipal = getCurrentUser();
        ClientProfile profile = clientProfileRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Client profile not found", "CLIENT_PROFILE_NOT_FOUND"));

        return mapClientToResponse(profile);
    }

    @Transactional
    public ClientProfileResponse updateClientProfile(UpdateClientProfileRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        ClientProfile profile = clientProfileRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Client profile not found", "CLIENT_PROFILE_NOT_FOUND"));

        if (request.getCompanyName() != null) {
            profile.setCompanyName(request.getCompanyName().trim());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio().trim());
        }

        profile = clientProfileRepository.save(profile);
        return mapClientToResponse(profile);
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private FreelancerProfileResponse mapFreelancerToResponse(FreelancerProfile profile) {
        FreelancerProfileResponse response = freelancerProfileMapper.toResponse(profile);
        if (profile.getSkills() != null) {
            response.setSkills(profile.getSkills().stream()
                    .map(Skill::getName)
                    .toList());
        } else {
            response.setSkills(new ArrayList<>());
        }
        return response;
    }

    private ClientProfileResponse mapClientToResponse(ClientProfile profile) {
        return clientProfileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public Page<FreelancerProfileResponse> searchFreelancers(
            BigDecimal minRating,
            BigDecimal maxRating,
            BigDecimal minHourlyRate,
            BigDecimal maxHourlyRate,
            String currency,
            List<String> skills,
            Pageable pageable) {
        List<String> skillNamesParam = (skills == null || skills.isEmpty()) ? null : skills;
        
        Page<FreelancerProfile> profiles = freelancerProfileRepository.searchFreelancers(
                minRating, maxRating, minHourlyRate, maxHourlyRate, currency, skillNamesParam, pageable);
        
        return profiles.map(this::mapFreelancerToResponse);
    }
}

