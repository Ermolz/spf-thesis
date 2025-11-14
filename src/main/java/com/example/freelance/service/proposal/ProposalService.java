package com.example.freelance.service.proposal;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ConflictException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.project.ProjectStatus;
import com.example.freelance.domain.proposal.Proposal;
import com.example.freelance.domain.proposal.ProposalStatus;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.dto.proposal.CreateProposalRequest;
import com.example.freelance.dto.proposal.ProposalResponse;
import com.example.freelance.dto.proposal.UpdateProposalRequest;
import com.example.freelance.dto.project.InviteFreelancerRequest;
import com.example.freelance.mapper.proposal.ProposalMapper;
import com.example.freelance.repository.project.ProjectRepository;
import com.example.freelance.repository.proposal.ProposalRepository;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.common.util.MdcUtil;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProposalService {
    private static final String PROJECT_RESOURCE_NAME = "Project";
    private static final String PROPOSAL_RESOURCE_NAME = "Proposal";
    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ProposalMapper proposalMapper;

    @Transactional
    public ProposalResponse createProposal(CreateProposalRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        FreelancerProfile freelancer = getFreelancerProfile(userPrincipal.getId());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new NotFoundException(PROJECT_RESOURCE_NAME, request.getProjectId().toString()));

        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new BadRequestException("Can only submit proposals to open projects", "PROJECT_NOT_OPEN");
        }

        if (project.getClient().getUser().getId().equals(userPrincipal.getId())) {
            throw new BadRequestException("Cannot submit proposal to your own project", "CANNOT_PROPOSE_TO_OWN_PROJECT");
        }

        if (proposalRepository.existsByProjectIdAndFreelancerId(project.getId(), freelancer.getId())) {
            throw new ConflictException("Proposal already exists for this project", "PROPOSAL_ALREADY_EXISTS");
        }

        Proposal proposal = new Proposal();
        proposal.setProject(project);
        proposal.setFreelancer(freelancer);
        proposal.setCoverLetter(request.getCoverLetter());
        proposal.setBidAmount(request.getBidAmount());
        proposal.setEstimatedDuration(request.getEstimatedDuration());
        proposal.setStatus(ProposalStatus.PENDING);

        proposal = proposalRepository.save(proposal);
        return mapToResponse(proposal);
    }

    @Transactional
    public ProposalResponse updateProposal(Long proposalId, UpdateProposalRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        FreelancerProfile freelancer = getFreelancerProfile(userPrincipal.getId());

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException(PROPOSAL_RESOURCE_NAME, proposalId.toString()));

        if (!proposal.getFreelancer().getId().equals(freelancer.getId())) {
            throw new ForbiddenException("Can only update your own proposals", "NOT_PROPOSAL_OWNER");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("Can only update pending proposals", "PROPOSAL_NOT_PENDING");
        }

        if (request.getCoverLetter() != null) {
            proposal.setCoverLetter(request.getCoverLetter());
        }
        if (request.getBidAmount() != null) {
            proposal.setBidAmount(request.getBidAmount());
        }
        if (request.getEstimatedDuration() != null) {
            proposal.setEstimatedDuration(request.getEstimatedDuration());
        }

        proposal = proposalRepository.save(proposal);
        return mapToResponse(proposal);
    }

    @Transactional(readOnly = true)
    public ProposalResponse getProposalById(Long proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException(PROPOSAL_RESOURCE_NAME, proposalId.toString()));

        UserPrincipal userPrincipal = getCurrentUser();
        boolean isOwner = proposal.getFreelancer().getUser().getId().equals(userPrincipal.getId());
        boolean isClient = proposal.getProject().getClient().getUser().getId().equals(userPrincipal.getId());

        if (!isOwner && !isClient) {
            throw new ForbiddenException("Access denied to this proposal", "ACCESS_DENIED");
        }

        return mapToResponse(proposal);
    }

    @Transactional(readOnly = true)
    public Page<ProposalResponse> getMyProposals(Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        FreelancerProfile freelancer = getFreelancerProfile(userPrincipal.getId());

        Page<Proposal> proposals = proposalRepository.findByFreelancerId(freelancer.getId(), pageable);
        return proposals.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProposalResponse> getProjectProposals(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(PROJECT_RESOURCE_NAME, projectId.toString()));

        UserPrincipal userPrincipal = getCurrentUser();
        if (!project.getClient().getUser().getId().equals(userPrincipal.getId())) {
            throw new ForbiddenException("Can only view proposals for your own projects", "NOT_PROJECT_OWNER");
        }

        Page<Proposal> proposals = proposalRepository.findByProjectId(projectId, pageable);
        return proposals.map(this::mapToResponse);
    }

    @Transactional
    public ProposalResponse acceptProposal(Long proposalId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException(PROPOSAL_RESOURCE_NAME, proposalId.toString()));

        Project project = proposal.getProject();

        if (!project.getClient().getUser().getId().equals(userPrincipal.getId())) {
            throw new ForbiddenException("Only project owner can accept proposals", "NOT_PROJECT_OWNER");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("Can only accept pending proposals", "PROPOSAL_NOT_PENDING");
        }

        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new BadRequestException("Project is not open for proposals", "PROJECT_NOT_OPEN");
        }

        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("ACCEPT_PROPOSAL");
        
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal = proposalRepository.save(proposal);
        
        log.info("Proposal accepted: proposalId={}, projectId={}, freelancerId={}, bidAmount={}", 
                proposalId, project.getId(), proposal.getFreelancer().getId(), proposal.getBidAmount());

        proposalRepository.findByProjectIdAndStatus(project.getId(), ProposalStatus.PENDING)
                .forEach(p -> {
                    if (!p.getId().equals(proposalId)) {
                        p.setStatus(ProposalStatus.REJECTED);
                        proposalRepository.save(p);
                        log.debug("Proposal rejected: proposalId={}, projectId={}", p.getId(), project.getId());
                    }
                });

        project.setStatus(ProjectStatus.IN_PROGRESS);
        log.info("Project status changed: projectId={}, oldStatus={}, newStatus={}", 
                project.getId(), ProjectStatus.OPEN, ProjectStatus.IN_PROGRESS);
        MdcUtil.clearCustomValues();
        projectRepository.save(project);

        return mapToResponse(proposal);
    }

    @Transactional
    public ProposalResponse rejectProposal(Long proposalId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException(PROPOSAL_RESOURCE_NAME, proposalId.toString()));

        Project project = proposal.getProject();

        if (!project.getClient().getUser().getId().equals(userPrincipal.getId())) {
            throw new ForbiddenException("Only project owner can reject proposals", "NOT_PROJECT_OWNER");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("Can only reject pending proposals", "PROPOSAL_NOT_PENDING");
        }

        ProposalStatus oldStatus = proposal.getStatus();
        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("REJECT_PROPOSAL");
        
        proposal.setStatus(ProposalStatus.REJECTED);
        proposal = proposalRepository.save(proposal);
        
        log.info("Proposal status changed: proposalId={}, projectId={}, freelancerId={}, oldStatus={}, newStatus={}, changedBy={}", 
                proposalId, project.getId(), proposal.getFreelancer().getId(), oldStatus, ProposalStatus.REJECTED, userPrincipal.getId());
        MdcUtil.clearCustomValues();
        
        return mapToResponse(proposal);
    }

    @Transactional
    public ProposalResponse withdrawProposal(Long proposalId) {
        UserPrincipal userPrincipal = getCurrentUser();
        FreelancerProfile freelancer = getFreelancerProfile(userPrincipal.getId());

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException(PROPOSAL_RESOURCE_NAME, proposalId.toString()));

        if (!proposal.getFreelancer().getId().equals(freelancer.getId())) {
            throw new ForbiddenException("Can only withdraw your own proposals", "NOT_PROPOSAL_OWNER");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("Can only withdraw pending proposals", "PROPOSAL_NOT_PENDING");
        }

        ProposalStatus oldStatus = proposal.getStatus();
        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("WITHDRAW_PROPOSAL");
        
        proposal.setStatus(ProposalStatus.WITHDRAWN);
        proposal = proposalRepository.save(proposal);
        
        log.info("Proposal status changed: proposalId={}, projectId={}, freelancerId={}, oldStatus={}, newStatus={}, changedBy={}", 
                proposalId, proposal.getProject().getId(), proposal.getFreelancer().getId(), oldStatus, ProposalStatus.WITHDRAWN, userPrincipal.getId());
        MdcUtil.clearCustomValues();
        
        return mapToResponse(proposal);
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private FreelancerProfile getFreelancerProfile(Long userId) {
        return freelancerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Only freelancers can submit proposals", "FREELANCER_PROFILE_REQUIRED"));
    }

    @Transactional
    public ProposalResponse inviteFreelancer(Long projectId, InviteFreelancerRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        ClientProfile client = getClientProfile(userPrincipal.getId());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(PROJECT_RESOURCE_NAME, projectId.toString()));

        if (!project.getClient().getId().equals(client.getId())) {
            throw new ForbiddenException("Can only invite freelancers to your own projects", "NOT_PROJECT_OWNER");
        }

        if (project.getStatus() != ProjectStatus.OPEN && project.getStatus() != ProjectStatus.DRAFT) {
            throw new BadRequestException("Can only invite freelancers to open or draft projects", "INVALID_PROJECT_STATUS");
        }

        FreelancerProfile freelancer = freelancerProfileRepository.findById(request.getFreelancerId())
                .orElseThrow(() -> new NotFoundException("FreelancerProfile", request.getFreelancerId().toString()));

        if (proposalRepository.existsByProjectIdAndFreelancerId(project.getId(), freelancer.getId())) {
            throw new ConflictException("Proposal already exists for this freelancer and project", "PROPOSAL_ALREADY_EXISTS");
        }

        // If project is DRAFT, publish it first
        if (project.getStatus() == ProjectStatus.DRAFT) {
            project.setStatus(ProjectStatus.OPEN);
            projectRepository.save(project);
        }

        Proposal proposal = new Proposal();
        proposal.setProject(project);
        proposal.setFreelancer(freelancer);
        proposal.setCoverLetter("Invitation from client: " + request.getMessage());
        proposal.setBidAmount(request.getSuggestedBidAmount() != null ? request.getSuggestedBidAmount() : project.getBudgetMin());
        proposal.setEstimatedDuration(request.getEstimatedDuration());
        proposal.setStatus(ProposalStatus.PENDING);

        proposal = proposalRepository.save(proposal);

        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("INVITE_FREELANCER");
        log.info("Freelancer invited: proposalId={}, projectId={}, freelancerId={}, clientId={}", 
                proposal.getId(), projectId, freelancer.getId(), client.getId());
        MdcUtil.clearCustomValues();

        return mapToResponse(proposal);
    }

    private ClientProfile getClientProfile(Long userId) {
        return clientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Only clients can invite freelancers", "CLIENT_PROFILE_REQUIRED"));
    }

    private ProposalResponse mapToResponse(Proposal proposal) {
        return proposalMapper.toResponse(proposal);
    }
}

