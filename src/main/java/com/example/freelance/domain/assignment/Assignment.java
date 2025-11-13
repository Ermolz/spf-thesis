package com.example.freelance.domain.assignment;

import com.example.freelance.common.domain.BaseEntity;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.proposal.Proposal;
import com.example.freelance.domain.user.FreelancerProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "assignments", uniqueConstraints = {
        @UniqueConstraint(columnNames = "project_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Assignment extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private FreelancerProfile freelancer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false, unique = true)
    private Proposal proposal;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;
}

