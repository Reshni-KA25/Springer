package com.kanini.springer.dto.Drive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating/updating candidates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRequest {
    
    private Long instituteId; // nullable for off-campus candidates
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private BigDecimal cgpa;
    private Integer historyOfArrears;
    private String degree;
    private String department;
    private Integer passoutYear;
    private LocalDate dateOfBirth;
    private String aadhaarNumber;
    private Boolean isEligible;
    private String reason; // Required when eligibility overridden
    private String status; // CandidateStatus enum string
}
