package com.example.freelance.service.assignment;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ConflictException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.assignment.AssignmentStatus;
import com.example.freelance.domain.proposal.Proposal;
import com.example.freelance.domain.proposal.ProposalStatus;
import com.example.freelance.dto.assignment.AssignmentResponse;
import com.example.freelance.dto.assignment.CreateAssignmentRequest;
import com.example.freelance.dto.assignment.UpdateAssignmentRequest;
import com.example.freelance.mapper.assignment.AssignmentMapper;
import com.example.freelance.repository.assignment.AssignmentRepository;
import com.example.freelance.repository.project.ProjectRepository;
import com.example.freelance.repository.proposal.ProposalRepository;
import com.example.freelance.common.util.MdcUtil;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final AssignmentMapper assignmentMapper;

    @Transactional
    public AssignmentResponse createAssignment(CreateAssignmentRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();

        Proposal proposal = proposalRepository.findById(request.getProposalId())
                .orElseThrow(() -> new NotFoundException("Proposal", request.getProposalId().toString()));

        if (proposal.getStatus() != ProposalStatus.ACCEPTED) {
            throw new BadRequestException("Can only create assignment from accepted proposal", "PROPOSAL_NOT_ACCEPTED");
        }

        if (!proposal.getProject().getClient().getUser().getId().equals(userPrincipal.getId())) {
            throw new ForbiddenException("Only project owner can create assignment", "NOT_PROJECT_OWNER");
        }

        if (assignmentRepository.existsByProjectId(proposal.getProject().getId())) {
            throw new ConflictException("Assignment already exists for this project", "ASSIGNMENT_ALREADY_EXISTS");
        }

        if (assignmentRepository.findByProposalId(proposal.getId()).isPresent()) {
            throw new ConflictException("Assignment already exists for this proposal", "ASSIGNMENT_ALREADY_EXISTS");
        }

        if (request.getStartDate().isBefore(Instant.now())) {
            throw new BadRequestException("Start date cannot be in the past", "INVALID_START_DATE");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date", "INVALID_END_DATE");
        }

        Assignment assignment = new Assignment();
        assignment.setProject(proposal.getProject());
        assignment.setFreelancer(proposal.getFreelancer());
        assignment.setProposal(proposal);
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());
        assignment.setStatus(AssignmentStatus.ACTIVE);

        assignment = assignmentRepository.save(assignment);
        return mapToResponse(assignment);
    }

    private static final String ASSIGNMENT_RESOURCE_NAME = "Assignment";

    @Transactional
    public AssignmentResponse updateAssignment(Long assignmentId, UpdateAssignmentRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException(ASSIGNMENT_RESOURCE_NAME, assignmentId.toString()));

        boolean isClient = assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = assignment.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this assignment", "ACCESS_DENIED");
        }

        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BadRequestException("Can only update active assignments", "ASSIGNMENT_NOT_ACTIVE");
        }

        if (request.getStartDate() != null) {
            if (request.getStartDate().isBefore(Instant.now())) {
                throw new BadRequestException("Start date cannot be in the past", "INVALID_START_DATE");
            }
            assignment.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            Instant startDate = request.getStartDate() != null ? request.getStartDate() : assignment.getStartDate();
            if (request.getEndDate().isBefore(startDate)) {
                throw new BadRequestException("End date cannot be before start date", "INVALID_END_DATE");
            }
            assignment.setEndDate(request.getEndDate());
        }

        assignment = assignmentRepository.save(assignment);
        return mapToResponse(assignment);
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException(ASSIGNMENT_RESOURCE_NAME, assignmentId.toString()));

        UserPrincipal userPrincipal = getCurrentUser();
        boolean isClient = assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = assignment.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this assignment", "ACCESS_DENIED");
        }

        return mapToResponse(assignment);
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentByProjectId(Long projectId) {
        Assignment assignment = assignmentRepository.findByProjectId(projectId)
                .orElseThrow(() -> new NotFoundException(ASSIGNMENT_RESOURCE_NAME, "for project " + projectId));

        UserPrincipal userPrincipal = getCurrentUser();
        boolean isClient = assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = assignment.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this assignment", "ACCESS_DENIED");
        }

        return mapToResponse(assignment);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getMyAssignments(Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        Page<Assignment> assignmentPage = assignmentRepository.findByFreelancerId(userPrincipal.getId(), pageable);
        return assignmentPage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getClientAssignments(Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        Page<Assignment> assignmentPage = assignmentRepository.findByClientId(userPrincipal.getId(), pageable);
        return assignmentPage.map(this::mapToResponse);
    }

    @Transactional
    public AssignmentResponse completeAssignment(Long assignmentId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException(ASSIGNMENT_RESOURCE_NAME, assignmentId.toString()));

        boolean isClient = assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = assignment.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this assignment", "ACCESS_DENIED");
        }

        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BadRequestException("Can only complete active assignments", "ASSIGNMENT_NOT_ACTIVE");
        }

        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("COMPLETE_ASSIGNMENT");
        
        AssignmentStatus oldStatus = assignment.getStatus();
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment = assignmentRepository.save(assignment);
        
        log.info("Assignment status changed: assignmentId={}, projectId={}, freelancerId={}, oldStatus={}, newStatus={}, changedBy={}", 
                assignmentId, assignment.getProject().getId(), assignment.getFreelancer().getId(), 
                oldStatus, AssignmentStatus.COMPLETED, userPrincipal.getId());
        MdcUtil.clearCustomValues();
        
        return mapToResponse(assignment);
    }

    @Transactional
    public AssignmentResponse cancelAssignment(Long assignmentId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException(ASSIGNMENT_RESOURCE_NAME, assignmentId.toString()));

        if (!assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId())) {
            throw new ForbiddenException("Only project owner can cancel assignment", "NOT_PROJECT_OWNER");
        }

        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BadRequestException("Can only cancel active assignments", "ASSIGNMENT_NOT_ACTIVE");
        }

        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("CANCEL_ASSIGNMENT");
        
        AssignmentStatus oldStatus = assignment.getStatus();
        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignment = assignmentRepository.save(assignment);
        
        log.info("Assignment status changed: assignmentId={}, projectId={}, freelancerId={}, oldStatus={}, newStatus={}, changedBy={}", 
                assignmentId, assignment.getProject().getId(), assignment.getFreelancer().getId(), 
                oldStatus, AssignmentStatus.CANCELLED, userPrincipal.getId());
        MdcUtil.clearCustomValues();
        
        return mapToResponse(assignment);
    }

    @Transactional
    public AssignmentResponse createAssignmentFromProposal(Long proposalId, Instant startDate, Instant endDate) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal", proposalId.toString()));

        if (proposal.getStatus() != ProposalStatus.ACCEPTED) {
            throw new BadRequestException("Can only create assignment from accepted proposal", "PROPOSAL_NOT_ACCEPTED");
        }

        if (assignmentRepository.existsByProjectId(proposal.getProject().getId())) {
            throw new ConflictException("Assignment already exists for this project", "ASSIGNMENT_ALREADY_EXISTS");
        }

        if (assignmentRepository.findByProposalId(proposal.getId()).isPresent()) {
            throw new ConflictException("Assignment already exists for this proposal", "ASSIGNMENT_ALREADY_EXISTS");
        }

        if (startDate == null) {
            startDate = Instant.now();
        }

        if (startDate.isBefore(Instant.now())) {
            throw new BadRequestException("Start date cannot be in the past", "INVALID_START_DATE");
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date", "INVALID_END_DATE");
        }

        Assignment assignment = new Assignment();
        assignment.setProject(proposal.getProject());
        assignment.setFreelancer(proposal.getFreelancer());
        assignment.setProposal(proposal);
        assignment.setStartDate(startDate);
        assignment.setEndDate(endDate);
        assignment.setStatus(AssignmentStatus.ACTIVE);

        assignment = assignmentRepository.save(assignment);
        return mapToResponse(assignment);
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private AssignmentResponse mapToResponse(Assignment assignment) {
        return assignmentMapper.toResponse(assignment);
    }
}

