package com.kanini.springer.dto.Drive;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating candidate details
 * Reason is mandatory for audit trail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateUpdateRequest {
    
    private Long instituteId; // nullable for off-campus candidates
    private Long cycleId; // nullable for candidates not yet assigned to a cycle
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private Long userId;
    private LocalDate dateOfBirth;
    private String aadhaarNumber;
    private Boolean isEligible; // Can be manually overridden
    
    @NotBlank(message = "Reason is required for any candidate update")
    private String reason; // Mandatory reason for update audit trail
}
