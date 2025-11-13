package com.example.freelance.service.user;

import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.dto.user.ClientProfileResponse;
import com.example.freelance.dto.user.FreelancerProfileResponse;
import com.example.freelance.dto.user.UpdateClientProfileRequest;
import com.example.freelance.dto.user.UpdateFreelancerProfileRequest;
import com.example.freelance.mapper.user.ClientProfileMapper;
import com.example.freelance.mapper.user.FreelancerProfileMapper;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.repository.user.UserRepository;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;
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
                .orElseThrow(() -> new ForbiddenException("Freelancer profile not found", "FREELANCER_PROFILE_NOT_FOUND"));

        return mapFreelancerToResponse(profile);
    }

    @Transactional
    public FreelancerProfileResponse updateFreelancerProfile(UpdateFreelancerProfileRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ForbiddenException("Freelancer profile not found", "FREELANCER_PROFILE_NOT_FOUND"));

        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getSkills() != null) {
            profile.setSkills(new ArrayList<>(request.getSkills()));
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
            profile.setCompanyName(request.getCompanyName());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        profile = clientProfileRepository.save(profile);
        return mapClientToResponse(profile);
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private FreelancerProfileResponse mapFreelancerToResponse(FreelancerProfile profile) {
        FreelancerProfileResponse response = freelancerProfileMapper.toResponse(profile);
        response.setSkills(profile.getSkills() != null ? new ArrayList<>(profile.getSkills()) : new ArrayList<>());
        return response;
    }

    private ClientProfileResponse mapClientToResponse(ClientProfile profile) {
        return clientProfileMapper.toResponse(profile);
    }
}

