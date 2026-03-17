package com.kanini.springer.entity.Drive;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.kanini.springer.entity.HiringReq.User;
import com.kanini.springer.entity.enums.Enums.ApplicationStatus;

/**
 * The candidate during the drive test they are moved to drive_application and they
 * are verified by the registration_code sent to their email
 */
@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id")
    private Drive drive;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;
    
    private String registrationCode; // unique if there is an entry, can be null for on-campus
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus; // IN_DRIVE, DROPPED, ROUND1 PASSED, ROUND2 PASSED, FAILED, SELECTED
    
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdByUser;
    
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    private List<CandidateEvaluation> candidateEvaluations;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
