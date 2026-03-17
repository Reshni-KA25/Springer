package com.kanini.springer.entity.Drive;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.kanini.springer.entity.Academy.BatchAllocation;
import com.kanini.springer.entity.DocumentProcessing.DocumentSubmission;
import com.kanini.springer.entity.DocumentProcessing.OfferLetter;
import com.kanini.springer.entity.HiringReq.Institute;
import com.kanini.springer.entity.enums.Enums.CandidateStatus;

/**
 * The candidates details who appear for the drive
 * UNIQUE = constraint + index
 */
@Entity
@Table(name = "candidates", uniqueConstraints = {

        @UniqueConstraint(name = "uk_candidate_email", columnNames = "email"),

        @UniqueConstraint(name = "uk_candidate_aadhaar", columnNames = "aadhaarNumber"),
        @UniqueConstraint(name = "uk_candidate_email", columnNames = "email"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institute_id")
    private Institute institute; // nullable if off-campus pool

    @NotBlank(message = "Candidate first name is required")
    @Pattern(regexp = "^[a-zA-Z\\s.]+$", message = "First name should contain only letters")
    @Size(min = 2, max = 50)
    private String firstName;

    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number should be a valid 10-digit Indian number")
    private String mobile;

    @Column(precision = 10, scale = 2)
    private BigDecimal cgpa;

    private Integer historyOfArrears; // no of arrears

    private String degree;

    private String department;

    private Integer passoutYear;

    private LocalDate dateOfBirth;

    @Pattern(regexp = "^\\d{12}$", message = "Aadhaar number must be exactly 12 digits")
    private String aadhaarNumber;

    private Boolean isEligible; // Candidate-level eligibility flag

    @Column(columnDefinition = "TEXT")
    private String eligibilityReason; // Required only when eligibility was overridden/exception

    @Enumerated(EnumType.STRING)
    private CandidateStatus status;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<CandidateSkill> candidateSkills;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Application> applications;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<DocumentSubmission> documentSubmissions;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<OfferLetter> offerLetters;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<BatchAllocation> batchAllocations;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
