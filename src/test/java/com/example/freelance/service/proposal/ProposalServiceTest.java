package com.example.freelance.service.proposal;

import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.project.ProjectStatus;
import com.example.freelance.domain.proposal.Proposal;
import com.example.freelance.domain.proposal.ProposalStatus;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.proposal.CreateProposalRequest;
import com.example.freelance.dto.proposal.ProposalResponse;
import com.example.freelance.mapper.proposal.ProposalMapper;
import com.example.freelance.repository.project.ProjectRepository;
import com.example.freelance.repository.proposal.ProposalRepository;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock private ProposalRepository proposalRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private FreelancerProfileRepository freelancerProfileRepository;
    @Mock private ClientProfileRepository clientProfileRepository;
    @Mock private ProposalMapper proposalMapper;

    private ProposalService proposalService;

    private static final Long AUTH_USER_ID = 10L;

    @BeforeEach
    void setUp() {
        proposalService = new ProposalService(
                proposalRepository,
                projectRepository,
                freelancerProfileRepository,
                clientProfileRepository,
                proposalMapper
        );

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserPrincipal principal = mock(UserPrincipal.class);

        when(principal.getId()).thenReturn(AUTH_USER_ID);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProposal_WhenProjectOpen_ShouldSuccess() {
        CreateProposalRequest request = new CreateProposalRequest(100L, "Hire me", new BigDecimal("500"), 5);

        User freelancerUser = new User(); freelancerUser.setId(AUTH_USER_ID);
        FreelancerProfile freelancerProfile = new FreelancerProfile();
        freelancerProfile.setId(1L);
        freelancerProfile.setUser(freelancerUser);

        User clientUser = new User(); clientUser.setId(99L);
        ClientProfile clientProfile = new ClientProfile(); clientProfile.setUser(clientUser);

        Project project = new Project();
        project.setId(100L);
        project.setStatus(ProjectStatus.OPEN);
        project.setClient(clientProfile);
        project.setBudgetMin(BigDecimal.ZERO);

        when(freelancerProfileRepository.findByUserId(AUTH_USER_ID)).thenReturn(Optional.of(freelancerProfile));
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

        lenient().when(proposalRepository.existsByProjectIdAndFreelancerId(100L, 1L)).thenReturn(false);

        when(proposalRepository.save(any(Proposal.class))).thenAnswer(i -> i.getArgument(0));
        when(proposalMapper.toResponse(any(Proposal.class))).thenReturn(ProposalResponse.builder().status(ProposalStatus.PENDING).build());

        ProposalResponse response = proposalService.createProposal(request);

        assertThat(response.getStatus()).isEqualTo(ProposalStatus.PENDING);
        verify(proposalRepository).save(any(Proposal.class));
    }

    @Test
    void acceptProposal_WhenOwnerAccepts_ShouldRejectOthersAndCloseProject() {
        Long proposalId = 1L;
        Long projectId = 100L;

        User clientUser = new User(); clientUser.setId(AUTH_USER_ID);
        ClientProfile clientProfile = new ClientProfile(); clientProfile.setUser(clientUser);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectStatus.OPEN);
        project.setClient(clientProfile);

        FreelancerProfile freelancer = new FreelancerProfile();
        freelancer.setId(5L);

        Proposal targetProposal = new Proposal();
        targetProposal.setId(proposalId);
        targetProposal.setProject(project);
        targetProposal.setFreelancer(freelancer);
        targetProposal.setStatus(ProposalStatus.PENDING);
        targetProposal.setBidAmount(new BigDecimal("500"));

        Proposal otherProposal = new Proposal();
        otherProposal.setId(2L);
        otherProposal.setStatus(ProposalStatus.PENDING);

        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(targetProposal));
        when(proposalRepository.findByProjectIdAndStatus(projectId, ProposalStatus.PENDING))
                .thenReturn(List.of(targetProposal, otherProposal));

        when(proposalRepository.save(any(Proposal.class))).thenAnswer(i -> i.getArgument(0));
        when(proposalMapper.toResponse(any(Proposal.class))).thenReturn(ProposalResponse.builder().status(ProposalStatus.ACCEPTED).build());

        proposalService.acceptProposal(proposalId);

        assertThat(targetProposal.getStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(otherProposal.getStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);

        verify(projectRepository).save(project);
    }
}