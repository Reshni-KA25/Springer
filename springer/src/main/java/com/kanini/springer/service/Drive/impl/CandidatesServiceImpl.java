package com.kanini.springer.service.Drive.impl;

import com.kanini.springer.dto.Common.FieldChangeDTO;
import com.kanini.springer.dto.Common.ManualOverrideRequest;
import com.kanini.springer.dto.Drive.BulkCandidateCreateResponse;
import com.kanini.springer.dto.Drive.BulkCandidateStatusUpdateRequest;
import com.kanini.springer.dto.Drive.BulkCandidateStatusUpdateResponse;
import com.kanini.springer.dto.Drive.CandidateRequest;
import com.kanini.springer.dto.Drive.CandidateResponse;
import com.kanini.springer.dto.Drive.CandidateStatusUpdateRequest;
import com.kanini.springer.dto.Drive.CandidateUpdateRequest;
import com.kanini.springer.dto.Drive.EligibilityValidationResult;
import com.kanini.springer.entity.Drive.Candidate;
import com.kanini.springer.entity.Drive.CandidateSkill;
import com.kanini.springer.entity.HiringReq.HiringCycle;
import com.kanini.springer.entity.HiringReq.Institute;
import com.kanini.springer.entity.HiringReq.Skill;
import com.kanini.springer.entity.HiringReq.User;
import com.kanini.springer.entity.enums.Enums.CandidateStatus;
import com.kanini.springer.entity.enums.Enums.CycleStatus;
import com.kanini.springer.mapper.Drive.CandidateMapper;
import com.kanini.springer.repository.Drive.CandidateSkillRepository;
import com.kanini.springer.repository.Drive.CandidatesRepository;
import com.kanini.springer.repository.Hiring.HiringCycleRepository;
import com.kanini.springer.repository.Hiring.InstituteRepository;
import com.kanini.springer.repository.Hiring.SkillRepository;
import com.kanini.springer.repository.Hiring.UserRepository;
import com.kanini.springer.service.Common.IOverrideService;
import com.kanini.springer.service.Drive.ICandidatesService;
import com.kanini.springer.service.Drive.IEligibilityRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CandidatesServiceImpl implements ICandidatesService {
    
    private final CandidatesRepository candidatesRepository;
    private final InstituteRepository instituteRepository;
    private final HiringCycleRepository hiringCycleRepository;
    private final CandidateMapper mapper;
    private final IOverrideService overrideService;
    private final IEligibilityRuleService eligibilityRuleService;
    private final SkillRepository skillRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public CandidateResponse createCandidate(CandidateRequest request) {
        // Validate required fields
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new RuntimeException("First name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getMobile() == null || request.getMobile().isBlank()) {
            throw new RuntimeException("Mobile number is required");
        }
        
        // Check for existing candidate by email or aadhaar
        Candidate existingCandidate = null;
        
        // Check by email
        existingCandidate = candidatesRepository.findByEmail(request.getEmail()).orElse(null);
        
        // If not found by email, check by aadhaar (if provided)
        if (existingCandidate == null && request.getAadhaarNumber() != null && !request.getAadhaarNumber().isBlank()) {
            existingCandidate = candidatesRepository.findByAadhaarNumber(request.getAadhaarNumber()).orElse(null);
        }
        
        // If candidate exists, verify it's the same person before reusing
        if (existingCandidate != null) {
            // Verify candidate identity by matching CGPA, DOB, Passout Year, and Institute
            List<String> mismatchedFields = new ArrayList<>();
            
            if (request.getCgpa() != null && existingCandidate.getCgpa() != null && 
                existingCandidate.getCgpa().compareTo(request.getCgpa()) != 0) {
                mismatchedFields.add("CGPA (existing: " + existingCandidate.getCgpa() + ", provided: " + request.getCgpa() + ")");
            }
            
            if (request.getDateOfBirth() != null && existingCandidate.getDateOfBirth() != null && 
                !existingCandidate.getDateOfBirth().equals(request.getDateOfBirth())) {
                mismatchedFields.add("Date of Birth");
            }
            
            if (request.getPassoutYear() != null && existingCandidate.getPassoutYear() != null && 
                !existingCandidate.getPassoutYear().equals(request.getPassoutYear())) {
                mismatchedFields.add("Passout Year (existing: " + existingCandidate.getPassoutYear() + ", provided: " + request.getPassoutYear() + ")");
            }
            
            if (request.getInstituteId() != null && existingCandidate.getInstitute() != null && 
                !existingCandidate.getInstitute().getInstituteId().equals(request.getInstituteId())) {
                mismatchedFields.add("Institute (existing ID: " + existingCandidate.getInstitute().getInstituteId() + ", provided ID: " + request.getInstituteId() + ")");
            }
            
            // If any fields don't match, reject with detailed error
            if (!mismatchedFields.isEmpty()) {
                String errorMsg = "Candidate with email/aadhaar already exists but data doesn't match. Mismatched fields: " + 
                                  String.join(", ", mismatchedFields) + 
                                  ". Please verify the candidate information.";
                throw new RuntimeException(errorMsg);
            }
            
            // Check if they're in the same cycle
            if (request.getCycleId() != null && existingCandidate.getCycle() != null && 
                existingCandidate.getCycle().getCycleId().equals(request.getCycleId())) {
                // Same cycle - reject
                throw new RuntimeException("Candidate already exists in this cycle with email: " + request.getEmail());
            }
            
            // Identity verified and different cycle - reuse existing candidate and update cycleId
            if (request.getCycleId() != null) {
                validateCycleIsOpen(request.getCycleId());
                HiringCycle newCycle = hiringCycleRepository.findById(request.getCycleId())
                        .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + request.getCycleId()));
                existingCandidate.setCycle(newCycle);
            }
            
            // Update only mutable fields (identity fields like CGPA, DOB, passout year, institute are already verified)
            existingCandidate.setFirstName(request.getFirstName());
            if (request.getLastName() != null) {
                existingCandidate.setLastName(request.getLastName());
            }
            existingCandidate.setMobile(request.getMobile());
            existingCandidate.setHistoryOfArrears(request.getHistoryOfArrears());
            if (request.getDegree() != null) {
                existingCandidate.setDegree(request.getDegree());
            }
            if (request.getDepartment() != null) {
                existingCandidate.setDepartment(request.getDepartment());
            }
            
            // Note: CGPA, DateOfBirth, PassoutYear, and Institute are NOT updated as they were used for identity verification
            
            // Re-check eligibility with new data
            EligibilityValidationResult eligibilityResult = eligibilityRuleService.checkEligibility(
                    existingCandidate.getCgpa(),
                    existingCandidate.getPassoutYear(),
                    existingCandidate.getHistoryOfArrears()
            );
            
            existingCandidate.setIsEligible(eligibilityResult.isEligible());
            
            if (!eligibilityResult.isEligible() && eligibilityResult.getFailedReasons() != null && !eligibilityResult.getFailedReasons().isEmpty()) {
                String reason = String.join("; ", eligibilityResult.getFailedReasons());
                existingCandidate.setReason(reason);
            } else {
                existingCandidate.setReason("");
            }
            
            // Reset status to APPLIED for new cycle
            existingCandidate.setStatus(CandidateStatus.APPLIED);
            
            // Save updated candidate (createdAt remains old, updatedAt gets updated automatically)
            Candidate savedCandidate = candidatesRepository.save(existingCandidate);
            
            // Update skills if provided
            if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
                // Remove old skills
                candidateSkillRepository.deleteAll(savedCandidate.getCandidateSkills());
                // Map new skills
                mapCandidateSkills(savedCandidate, request.getSkillIds());
            }
            
            // Reload candidate with skills
            savedCandidate = candidatesRepository.findByIdWithInstitute(savedCandidate.getCandidateId())
                    .orElseThrow(() -> new RuntimeException("Error reloading candidate"));
            
            return mapper.toResponse(savedCandidate);
        }
        
        // No existing candidate - create new one
        // Validate cycle status if cycleId is provided
        if (request.getCycleId() != null) {
            validateCycleIsOpen(request.getCycleId());
        }
        
        Candidate candidate = mapper.toEntity(request);
        
        // Check eligibility based on rules
        EligibilityValidationResult eligibilityResult = eligibilityRuleService.checkEligibility(
                candidate.getCgpa(),
                candidate.getPassoutYear(),
                candidate.getHistoryOfArrears()
        );
        
        candidate.setIsEligible(eligibilityResult.isEligible());
        
        if (!eligibilityResult.isEligible() && eligibilityResult.getFailedReasons() != null && !eligibilityResult.getFailedReasons().isEmpty()) {
            String reason = String.join("; ", eligibilityResult.getFailedReasons());
            candidate.setReason(reason);
        }
        
        Candidate savedCandidate = candidatesRepository.save(candidate);
        
        // Map candidate skills
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            mapCandidateSkills(savedCandidate, request.getSkillIds());
        }
        
        // Reload candidate with skills
        savedCandidate = candidatesRepository.findByIdWithInstitute(savedCandidate.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Error reloading candidate"));
        
        return mapper.toResponse(savedCandidate);
    }
    
    @Override
    @Transactional
 
    public BulkCandidateCreateResponse bulkCreateCandidates(List<CandidateRequest> requests) {
        BulkCandidateCreateResponse response = new BulkCandidateCreateResponse();
        response.setTotalProcessed(requests.size());
        
        if (requests == null || requests.isEmpty()) {
            response.getErrorMessages().add("Request list cannot be empty");
            response.setFailureCount(1);
            return response;
        }
        
        // Phase 1: Validate all candidates before inserting any
        List<String> validationErrors = new ArrayList<>();
        Set<String> emailsInBatch = new HashSet<>();
        Set<String> aadhaarsInBatch = new HashSet<>();
        List<Candidate> candidatesToUpdate = new ArrayList<>();
        List<Integer> updateIndices = new ArrayList<>();
        
        for (int i = 0; i < requests.size(); i++) {
            CandidateRequest request = requests.get(i);
            String candidateRef = "Candidate #" + (i + 1);
            
            // Validate email uniqueness within batch
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                validationErrors.add(candidateRef + ": Email is required");
            } else {
                if (emailsInBatch.contains(request.getEmail().toLowerCase())) {
                    validationErrors.add(candidateRef + ": Duplicate email within batch - " + request.getEmail());
                } else {
                    emailsInBatch.add(request.getEmail().toLowerCase());
                    
                    // Check against database - if exists, verify identity then check cycle
                    Candidate existingCandidate = candidatesRepository.findByEmail(request.getEmail()).orElse(null);
                    if (existingCandidate != null) {
                        // Verify candidate identity before reusing
                        List<String> mismatchedFields = new ArrayList<>();
                        
                        if (request.getCgpa() != null && existingCandidate.getCgpa() != null && 
                            existingCandidate.getCgpa().compareTo(request.getCgpa()) != 0) {
                            mismatchedFields.add("CGPA");
                        }
                        
                        if (request.getDateOfBirth() != null && existingCandidate.getDateOfBirth() != null && 
                            !existingCandidate.getDateOfBirth().equals(request.getDateOfBirth())) {
                            mismatchedFields.add("Date of Birth");
                        }
                        
                        if (request.getPassoutYear() != null && existingCandidate.getPassoutYear() != null && 
                            !existingCandidate.getPassoutYear().equals(request.getPassoutYear())) {
                            mismatchedFields.add("Passout Year");
                        }
                        
                        if (request.getInstituteId() != null && existingCandidate.getInstitute() != null && 
                            !existingCandidate.getInstitute().getInstituteId().equals(request.getInstituteId())) {
                            mismatchedFields.add("Institute");
                        }
                        
                        // If fields don't match, add validation error
                        if (!mismatchedFields.isEmpty()) {
                            validationErrors.add(candidateRef + ": Email exists but candidate data doesn't match (" + 
                                               String.join(", ", mismatchedFields) + " mismatch) - " + request.getEmail());
                        } else {
                            // Identity verified - check if same cycle
                            if (request.getCycleId() != null && existingCandidate.getCycle() != null && 
                                existingCandidate.getCycle().getCycleId().equals(request.getCycleId())) {
                                validationErrors.add(candidateRef + ": Email already exists in same cycle - " + request.getEmail());
                            } else {
                                // Different cycle - mark for update
                                candidatesToUpdate.add(existingCandidate);
                                updateIndices.add(i);
                            }
                        }
                    }
                }
            }
            
            // Validate aadhaar uniqueness within batch and database (if provided)
            if (request.getAadhaarNumber() != null && !request.getAadhaarNumber().isBlank()) {
                if (aadhaarsInBatch.contains(request.getAadhaarNumber())) {
                    validationErrors.add(candidateRef + ": Duplicate Aadhaar within batch - " + request.getAadhaarNumber());
                } else {
                    aadhaarsInBatch.add(request.getAadhaarNumber());
                    
                    // Check against database - if exists, verify identity then check cycle
                    Candidate existingByAadhaar = candidatesRepository.findByAadhaarNumber(request.getAadhaarNumber()).orElse(null);
                    if (existingByAadhaar != null) {
                        // Check if already marked for update by email
                        boolean alreadyMarkedForUpdate = candidatesToUpdate.stream()
                            .anyMatch(c -> c.getCandidateId().equals(existingByAadhaar.getCandidateId()));
                        
                        if (!alreadyMarkedForUpdate) {
                            // Verify candidate identity before reusing
                            List<String> mismatchedFields = new ArrayList<>();
                            
                            if (request.getCgpa() != null && existingByAadhaar.getCgpa() != null && 
                                existingByAadhaar.getCgpa().compareTo(request.getCgpa()) != 0) {
                                mismatchedFields.add("CGPA");
                            }
                            
                            if (request.getDateOfBirth() != null && existingByAadhaar.getDateOfBirth() != null && 
                                !existingByAadhaar.getDateOfBirth().equals(request.getDateOfBirth())) {
                                mismatchedFields.add("Date of Birth");
                            }
                            
                            if (request.getPassoutYear() != null && existingByAadhaar.getPassoutYear() != null && 
                                !existingByAadhaar.getPassoutYear().equals(request.getPassoutYear())) {
                                mismatchedFields.add("Passout Year");
                            }
                            
                            if (request.getInstituteId() != null && existingByAadhaar.getInstitute() != null && 
                                !existingByAadhaar.getInstitute().getInstituteId().equals(request.getInstituteId())) {
                                mismatchedFields.add("Institute");
                            }
                            
                            // If fields don't match, add validation error
                            if (!mismatchedFields.isEmpty()) {
                                validationErrors.add(candidateRef + ": Aadhaar exists but candidate data doesn't match (" + 
                                                   String.join(", ", mismatchedFields) + " mismatch) - " + request.getAadhaarNumber());
                            } else {
                                // Identity verified - check if same cycle
                                if (request.getCycleId() != null && existingByAadhaar.getCycle() != null && 
                                    existingByAadhaar.getCycle().getCycleId().equals(request.getCycleId())) {
                                    validationErrors.add(candidateRef + ": Aadhaar already exists in same cycle - " + request.getAadhaarNumber());
                                } else {
                                    // Different cycle - mark for update
                                    candidatesToUpdate.add(existingByAadhaar);
                                    updateIndices.add(i);
                                }
                            }
                        }
                    }
                }
            }
            
            // Validate cycle if provided
            if (request.getCycleId() != null) {
                try {
                    validateCycleIsOpen(request.getCycleId());
                } catch (Exception e) {
                    validationErrors.add(candidateRef + ": " + e.getMessage());
                }
            }
            
            // Validate institute exists
            if (request.getInstituteId() != null) {
                if (!instituteRepository.existsById(request.getInstituteId())) {
                    validationErrors.add(candidateRef + ": Institute not found with ID: " + request.getInstituteId());
                }
            }
            
            // Validate required fields
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                validationErrors.add(candidateRef + ": First name is required");
            }
            if (request.getCgpa() == null) {
                validationErrors.add(candidateRef + ": CGPA is required");
            }
            if (request.getPassoutYear() == null) {
                validationErrors.add(candidateRef + ": Passout year is required");
            }
        }
        
        // If any validation errors, return failure response with no inserts
        if (!validationErrors.isEmpty()) {
            response.setErrorMessages(validationErrors);
            response.setFailureCount(requests.size());
            response.setSuccessCount(0);
            return response;
        }
        
        // Phase 2: All validations passed - proceed with inserts and updates
        List<Candidate> candidatesToInsert = new ArrayList<>();
        List<CandidateRequest> insertRequests = new ArrayList<>();
        List<CandidateRequest> updateRequests = new ArrayList<>();
        
        for (int i = 0; i < requests.size(); i++) {
            CandidateRequest request = requests.get(i);
            
            // Check if this is an update or insert
            if (updateIndices.contains(i)) {
                updateRequests.add(request);
            } else {
                // New candidate - create entity
                try {
                    Candidate candidate = mapper.toEntity(request);
                    
                    // Check eligibility based on rules
                    EligibilityValidationResult eligibilityResult = eligibilityRuleService.checkEligibility(
                            candidate.getCgpa(),
                            candidate.getPassoutYear(),
                            candidate.getHistoryOfArrears()
                    );
                    
                    candidate.setIsEligible(eligibilityResult.isEligible());
                    
                    if (!eligibilityResult.isEligible() && eligibilityResult.getFailedReasons() != null && !eligibilityResult.getFailedReasons().isEmpty()) {
                        String reason = String.join("; ", eligibilityResult.getFailedReasons());
                        candidate.setReason(reason);
                    }
                    
                    candidatesToInsert.add(candidate);
                    insertRequests.add(request);
                } catch (Exception e) {
                    // This shouldn't happen as we validated already, but handle gracefully
                    throw new RuntimeException("Error creating candidate entity: " + e.getMessage(), e);
                }
            }
        }
        
        // Update existing candidates
        for (int i = 0; i < candidatesToUpdate.size(); i++) {
            Candidate existingCandidate = candidatesToUpdate.get(i);
            CandidateRequest request = updateRequests.get(i);
            
            // Update cycleId if provided
            if (request.getCycleId() != null) {
                HiringCycle newCycle = hiringCycleRepository.findById(request.getCycleId())
                        .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + request.getCycleId()));
                existingCandidate.setCycle(newCycle);
            }
            
            // Update only mutable fields (identity fields like CGPA, DOB, passout year, institute were verified)
            existingCandidate.setFirstName(request.getFirstName());
            if (request.getLastName() != null) {
                existingCandidate.setLastName(request.getLastName());
            }
            existingCandidate.setMobile(request.getMobile());
            existingCandidate.setHistoryOfArrears(request.getHistoryOfArrears());
            if (request.getDegree() != null) {
                existingCandidate.setDegree(request.getDegree());
            }
            if (request.getDepartment() != null) {
                existingCandidate.setDepartment(request.getDepartment());
            }
            
            // Note: CGPA, DateOfBirth, PassoutYear, and Institute are NOT updated as they were used for identity verification
            
            // Re-check eligibility with existing CGPA (no change expected since verified)
            EligibilityValidationResult eligibilityResult = eligibilityRuleService.checkEligibility(
                    existingCandidate.getCgpa(),
                    existingCandidate.getPassoutYear(),
                    existingCandidate.getHistoryOfArrears()
            );
            
            existingCandidate.setIsEligible(eligibilityResult.isEligible());
            
            if (!eligibilityResult.isEligible() && eligibilityResult.getFailedReasons() != null && !eligibilityResult.getFailedReasons().isEmpty()) {
                String reason = String.join("; ", eligibilityResult.getFailedReasons());
                existingCandidate.setReason(reason);
            } else {
                existingCandidate.setReason("");
            }
            
            // Reset status to APPLIED for new cycle
            existingCandidate.setStatus(CandidateStatus.APPLIED);
            
            // Update skills if provided
            if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
                // Remove old skills
                candidateSkillRepository.deleteAll(existingCandidate.getCandidateSkills());
            }
        }
        
        // Save all candidates (both new and updated)
        List<Candidate> allCandidatesToSave = new ArrayList<>();
        allCandidatesToSave.addAll(candidatesToUpdate);
        allCandidatesToSave.addAll(candidatesToInsert);
        List<Candidate> savedCandidates = candidatesRepository.saveAll(allCandidatesToSave);
        
        // Map skills for each saved candidate
        List<CandidateRequest> allRequests = new ArrayList<>();
        allRequests.addAll(updateRequests);
        allRequests.addAll(insertRequests);
        
        for (int i = 0; i < savedCandidates.size(); i++) {
            Candidate savedCandidate = savedCandidates.get(i);
            CandidateRequest request = allRequests.get(i);
            
            if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
                try {
                    mapCandidateSkills(savedCandidate, request.getSkillIds());
                } catch (Exception e) {
                    // Log but don't fail the entire batch
                    System.err.println("Warning: Error mapping skills for candidate " + savedCandidate.getCandidateId() + ": " + e.getMessage());
                }
            }
        }
        
        // Reload all candidates with skills and institute details
        List<Long> savedIds = savedCandidates.stream()
                .map(Candidate::getCandidateId)
                .collect(java.util.stream.Collectors.toList());
        
        List<Candidate> reloadedCandidates = savedIds.stream()
                .map(id -> candidatesRepository.findByIdWithInstitute(id).orElse(null))
                .filter(c -> c != null)
                .collect(java.util.stream.Collectors.toList());
        
        // Build success response
        List<CandidateResponse> candidateResponses = mapper.toResponseList(reloadedCandidates);
        response.setSuccessfulInserts(candidateResponses);
        response.setSuccessCount(candidateResponses.size());
        response.setFailureCount(0);
        
        return response;
    }
    
    @Override
    public List<CandidateResponse> getAllCandidates() {
        List<Candidate> candidates = candidatesRepository.findAllWithInstitute();
        return mapper.toResponseList(candidates);
    }
    
    @Override
    public CandidateResponse getCandidateById(Long candidateId) {
        Candidate candidate = candidatesRepository.findByIdWithInstitute(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found with ID: " + candidateId));
        return mapper.toResponse(candidate);
    }
    
    @Override
    public List<CandidateResponse> getCandidatesByInstituteId(Long instituteId) {
        // Validate institute exists
        if (!instituteRepository.existsById(instituteId)) {
            throw new RuntimeException("Institute not found with ID: " + instituteId);
        }
        
        List<Candidate> candidates = candidatesRepository.findByInstituteIdWithInstitute(instituteId);
        return mapper.toResponseList(candidates);
    }
    
    @Override
    public List<CandidateResponse> getCandidatesByCycleId(Long cycleId) {
        // Validate cycle exists
        if (!hiringCycleRepository.existsById(cycleId)) {
            throw new RuntimeException("Hiring cycle not found with ID: " + cycleId);
        }
        
        List<Candidate> candidates = candidatesRepository.findByCycleIdWithDetails(cycleId);
        return mapper.toResponseList(candidates);
    }
    
    @Override
    @Transactional
    public CandidateResponse updateCandidate(Long candidateId, CandidateUpdateRequest request, Long updatedBy) {
        // Validate mandatory reason field
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new RuntimeException("Reason is required for candidate update");
        }
        
        // Fetch old state
        Candidate oldCandidate = candidatesRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found with ID: " + candidateId));
        
        // Create a copy for change detection
        Candidate oldCandidateCopy = createCandidateCopy(oldCandidate);
        
        // Update fields (partial update)
        if (request.getInstituteId() != null) {
            Institute institute = instituteRepository.findById(request.getInstituteId())
                    .orElseThrow(() -> new RuntimeException("Institute not found with ID: " + request.getInstituteId()));
            oldCandidate.setInstitute(institute);
        }
        
        if (request.getCycleId() != null) {
            // Validate that the new cycle is OPEN
            validateCycleIsOpen(request.getCycleId());
            
            HiringCycle cycle = hiringCycleRepository.findById(request.getCycleId())
                    .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + request.getCycleId()));
            oldCandidate.setCycle(cycle);
        }
        
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            oldCandidate.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            oldCandidate.setLastName(request.getLastName());
        }
        
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if email is unique (excluding current candidate)
            candidatesRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getCandidateId().equals(candidateId)) {
                    throw new RuntimeException("Email already exists: " + request.getEmail());
                }
            });
            oldCandidate.setEmail(request.getEmail());
        }
        
        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            oldCandidate.setMobile(request.getMobile());
        }
        
        
        
        if (request.getDateOfBirth() != null) {
            oldCandidate.setDateOfBirth(request.getDateOfBirth());
        }
        
        if (request.getAadhaarNumber() != null) {
            oldCandidate.setAadhaarNumber(request.getAadhaarNumber());
        }
        
        // isEligible can be manually overridden (e.g., exception cases)
        if (request.getIsEligible() != null) {
            oldCandidate.setIsEligible(request.getIsEligible());
        }
        
        // Note: status is not updated via CandidateUpdateRequest
        // Use updateCandidateStatus() for status changes
        
        // Detect changes
        List<FieldChangeDTO> changes = overrideService.detectChanges(oldCandidateCopy, oldCandidate);
        
        // Save updated candidate
        Candidate updatedCandidate = candidatesRepository.save(oldCandidate);
        
        // Log override if there are changes and updatedBy is provided
        if (!changes.isEmpty() && updatedBy != null) {
            ManualOverrideRequest overrideRequest = new ManualOverrideRequest();
            overrideRequest.setEntityType("CANDIDATES");
            overrideRequest.setEntityId(candidateId);
            overrideRequest.setChanges(changes);
            overrideRequest.setOverrideReason(request.getReason()); // Use reason from request
            overrideRequest.setCreatedBy(updatedBy);
            
            try {
                overrideService.logOverride(overrideRequest);
            } catch (Exception e) {
                // Log error but don't fail the update
                System.err.println("Error logging override: " + e.getMessage());
            }
        }
        
        return mapper.toResponse(updatedCandidate);
    }
    
    @Override
    @Transactional
    public CandidateResponse updateCandidateStatus(Long candidateId, CandidateStatusUpdateRequest request) {
        Candidate candidate = candidatesRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found with ID: " + candidateId));
        
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new RuntimeException("Status is required");
        }
        
        // Parse the new status
        CandidateStatus newStatus = CandidateStatus.valueOf(request.getStatus());
        
        // Check if the candidate is eligible for status progression
        // Only eligible candidates can be SHORTLISTED, SCHEDULED, SELECTED, REJECTED, OFFERED, or JOINED
        if (!candidate.getIsEligible() && 
            (newStatus == CandidateStatus.SHORTLISTED ||
             newStatus == CandidateStatus.SCHEDULED ||
             newStatus == CandidateStatus.SELECTED ||
             newStatus == CandidateStatus.REJECTED ||
             newStatus == CandidateStatus.OFFERED ||
             newStatus == CandidateStatus.JOINED)) {
            throw new RuntimeException("Cannot update status to " + newStatus + ". Candidate is not eligible. Only eligible candidates can progress in recruitment.");
        }
        
        // Create copy for change detection
        Candidate oldCandidate = createCandidateCopy(candidate);
        
        // Update status
        CandidateStatus oldStatus = candidate.getStatus();
        candidate.setStatus(newStatus);
        
        // Fetch username and update reason field
        String userName = "Unknown";
        if (request.getUpdatedBy() != null) {
            User user = userRepository.findById(request.getUpdatedBy()).orElse(null);
            if (user != null) {
                userName = user.getUsername();
            }
        }
        
        // Append reason to existing reason with format: . "The 'STATUS' update by userName"
        String statusUpdateReason = ". The \"" + newStatus + "\" update by " + userName;
        
        if (candidate.getReason() != null && !candidate.getReason().isBlank()) {
            candidate.setReason(candidate.getReason() + statusUpdateReason);
        } else {
            // If no existing reason, just set the update reason without leading dot
            candidate.setReason("The \"" + newStatus + "\" update by " + userName);
        }
        
        
        // Save
        Candidate updatedCandidate = candidatesRepository.save(candidate);
        
        // Log override if updatedBy is provided
        if (request.getUpdatedBy() != null) {
            List<FieldChangeDTO> changes = new ArrayList<>();
            FieldChangeDTO statusChange = new FieldChangeDTO();
            statusChange.setField("status");
            statusChange.setOld(oldStatus != null ? oldStatus.toString() : null);
            statusChange.setNewValue(newStatus.toString());
            changes.add(statusChange);
            
            ManualOverrideRequest overrideRequest = new ManualOverrideRequest();
            overrideRequest.setEntityType("CANDIDATES");
            overrideRequest.setEntityId(candidateId);
            overrideRequest.setChanges(changes);
            overrideRequest.setOverrideReason("Status update");
            overrideRequest.setCreatedBy(request.getUpdatedBy());
            
            try {
                overrideService.logOverride(overrideRequest);
            } catch (Exception e) {
                System.err.println("Error logging status override: " + e.getMessage());
            }
        }
        
        return mapper.toResponse(updatedCandidate);
    }
    
    @Override
    @Transactional
    public BulkCandidateStatusUpdateResponse bulkUpdateCandidateStatus(BulkCandidateStatusUpdateRequest request) {
        BulkCandidateStatusUpdateResponse response = new BulkCandidateStatusUpdateResponse();
        
        if (request.getCandidateIds() == null || request.getCandidateIds().isEmpty()) {
            throw new RuntimeException("Candidate IDs list cannot be empty");
        }
        
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new RuntimeException("Status is required");
        }
        
        // Parse and validate the new status
        CandidateStatus newStatus;
        try {
            newStatus = CandidateStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + request.getStatus());
        }
        
        // Fetch username if updatedBy is provided
        String userName = "Unknown";
        if (request.getUpdatedBy() != null) {
            User user = userRepository.findById(request.getUpdatedBy()).orElse(null);
            if (user != null) {
                userName = user.getUsername();
            }
        }
        
        int totalProcessed = 0;
        int successCount = 0;
        int failureCount = 0;
        
        for (Long candidateId : request.getCandidateIds()) {
            totalProcessed++;
            
            try {
                // Find candidate
                Candidate candidate = candidatesRepository.findById(candidateId).orElse(null);
                
                if (candidate == null) {
                    response.getErrorMessages().add("Candidate with ID " + candidateId + " not found");
                    failureCount++;
                    continue;
                }
                
                // Check eligibility for progression statuses
                if (!candidate.getIsEligible() && 
                    (newStatus == CandidateStatus.SHORTLISTED ||
                     newStatus == CandidateStatus.SCHEDULED ||
                     newStatus == CandidateStatus.SELECTED ||
                     newStatus == CandidateStatus.REJECTED ||
                     newStatus == CandidateStatus.OFFERED ||
                     newStatus == CandidateStatus.JOINED)) {
                    
                    String candidateName = candidate.getFirstName() + 
                            (candidate.getLastName() != null ? " " + candidate.getLastName() : "");
                    response.getErrorMessages().add(candidateName + " is ineligible, status cannot be updated to next level");
                    failureCount++;
                    continue;
                }
                
                // Update status
                CandidateStatus oldStatus = candidate.getStatus();
                candidate.setStatus(newStatus);
                
                // Append reason to existing reason with format: . "The 'STATUS' update by userName"
                String statusUpdateReason = ". The \"" + newStatus + "\" update by " + userName;
                
                if (candidate.getReason() != null && !candidate.getReason().isBlank()) {
                    candidate.setReason(candidate.getReason() + statusUpdateReason);
                } else {
                    // If no existing reason, just set the update reason without leading dot
                    candidate.setReason("The \"" + newStatus + "\" update by " + userName);
                }
                
                // Save candidate
                candidatesRepository.save(candidate);
                
               
                
                response.getSuccessfulCandidateIds().add(candidateId);
                successCount++;
                
            } catch (Exception e) {
                response.getErrorMessages().add("Error updating candidate " + candidateId + ": " + e.getMessage());
                failureCount++;
            }
        }
        
        response.setTotalProcessed(totalProcessed);
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        
        return response;
    }
    
   
    
    /**
     * Helper method to create a copy of candidate for change detection
     */
    private Candidate createCandidateCopy(Candidate original) {
        Candidate copy = new Candidate();
        copy.setCandidateId(original.getCandidateId());
        copy.setInstitute(original.getInstitute());
        copy.setFirstName(original.getFirstName());
        copy.setLastName(original.getLastName());
        copy.setEmail(original.getEmail());
        copy.setMobile(original.getMobile());
        copy.setCgpa(original.getCgpa());
        copy.setHistoryOfArrears(original.getHistoryOfArrears());
        copy.setDegree(original.getDegree());
        copy.setDepartment(original.getDepartment());
        copy.setPassoutYear(original.getPassoutYear());
        copy.setDateOfBirth(original.getDateOfBirth());
        copy.setAadhaarNumber(original.getAadhaarNumber());
        copy.setIsEligible(original.getIsEligible());
        copy.setReason(original.getReason());
        copy.setStatus(original.getStatus());
        return copy;
    }
    
    /**
     * Helper method to validate that a hiring cycle is OPEN
     * @param cycleId Cycle ID to validate
     * @throws RuntimeException if cycle not found or not OPEN
     */
    private void validateCycleIsOpen(Long cycleId) {
        HiringCycle cycle = hiringCycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + cycleId));
        
        if (cycle.getStatus() != CycleStatus.OPEN) {
            throw new RuntimeException("Cannot add candidates to cycle " + cycleId + ". Cycle status is " + cycle.getStatus() + ". Only OPEN cycles accept new candidates.");
        }
    }
    
    /**
     * Helper method to map candidate skills
     */
    private void mapCandidateSkills(Candidate candidate, List<Long> skillIds) {
        for (Long skillId : skillIds) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new RuntimeException("Skill not found with ID: " + skillId));
            
            CandidateSkill candidateSkill = new CandidateSkill();
            candidateSkill.setCandidate(candidate);
            candidateSkill.setSkill(skill);
            
            candidateSkillRepository.save(candidateSkill);
        }
    }
}

