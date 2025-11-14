package com.example.freelance.service.user;

import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.domain.assignment.AssignmentStatus;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.dto.user.FreelancerProfileResponse;
import com.example.freelance.mapper.user.FreelancerProfileMapper;
import com.example.freelance.repository.assignment.AssignmentRepository;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final AssignmentRepository assignmentRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final FreelancerProfileMapper freelancerProfileMapper;

    @Transactional(readOnly = true)
    public Page<FreelancerProfileResponse> getVerifiedFreelancers(Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        getClientProfile(userPrincipal.getId());

        var assignments = assignmentRepository.findByClientIdAndStatus(
                userPrincipal.getId(), AssignmentStatus.COMPLETED, Pageable.unpaged());

        Set<Long> seenFreelancerIds = new HashSet<>();
        List<FreelancerProfileResponse> freelancers = new ArrayList<>();
        
        for (var assignment : assignments) {
            Long freelancerId = assignment.getFreelancer().getId();
            if (!seenFreelancerIds.contains(freelancerId)) {
                FreelancerProfileResponse response = mapFreelancerToResponse(assignment.getFreelancer());
                freelancers.add(response);
                seenFreelancerIds.add(freelancerId);
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), freelancers.size());
        List<FreelancerProfileResponse> pageContent = start < freelancers.size() 
                ? freelancers.subList(start, end) 
                : new ArrayList<>();

        return new org.springframework.data.domain.PageImpl<>(
                pageContent, 
                pageable, 
                freelancers.size()
        );
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private ClientProfile getClientProfile(Long userId) {
        return clientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Client profile not found", "CLIENT_PROFILE_NOT_FOUND"));
    }

    private FreelancerProfileResponse mapFreelancerToResponse(FreelancerProfile profile) {
        FreelancerProfileResponse response = freelancerProfileMapper.toResponse(profile);
        response.setSkills(profile.getSkills() != null ? new ArrayList<>(profile.getSkills()) : new ArrayList<>());
        return response;
    }
}

