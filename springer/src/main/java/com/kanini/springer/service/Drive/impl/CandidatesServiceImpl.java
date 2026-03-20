package com.kanini.springer.service.Drive.impl;

import com.kanini.springer.dto.Common.FieldChangeDTO;
import com.kanini.springer.dto.Common.ManualOverrideRequest;
import com.kanini.springer.dto.Drive.CandidateRequest;
import com.kanini.springer.dto.Drive.CandidateResponse;
import com.kanini.springer.dto.Drive.CandidateStatusUpdateRequest;
import com.kanini.springer.entity.Drive.Candidate;
import com.kanini.springer.entity.HiringReq.Institute;
import com.kanini.springer.entity.enums.Enums.CandidateStatus;
import com.kanini.springer.mapper.Drive.CandidateMapper;
import com.kanini.springer.repository.Drive.CandidatesRepository;
import com.kanini.springer.repository.Hiring.InstituteRepository;
import com.kanini.springer.service.Common.IOverrideService;
import com.kanini.springer.service.Drive.ICandidatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidatesServiceImpl implements ICandidatesService {
    
    private final CandidatesRepository candidatesRepository;
    private final InstituteRepository instituteRepository;
    private final CandidateMapper mapper;
    private final IOverrideService overrideService;
    
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
        
        // Check for duplicate email
        if (candidatesRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Candidate already exists with email: " + request.getEmail());
        }
        
        // Check for duplicate aadhaar if provided
        if (request.getAadhaarNumber() != null && !request.getAadhaarNumber().isBlank()) {
            if (candidatesRepository.findByAadhaarNumber(request.getAadhaarNumber()).isPresent()) {
                throw new RuntimeException("Candidate already exists with Aadhaar number: " + request.getAadhaarNumber());
            }
        }
        
        Candidate candidate = mapRequestToEntity(request);
        Candidate savedCandidate = candidatesRepository.save(candidate);
        
        return mapper.toResponse(savedCandidate);
    }
    
    @Override
    @Transactional
    public List<CandidateResponse> bulkCreateCandidates(List<CandidateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("Request list cannot be empty");
        }
        
        List<Candidate> candidates = new ArrayList<>();
        
        for (CandidateRequest request : requests) {
            // Skip if email already exists
            if (candidatesRepository.findByEmail(request.getEmail()).isPresent()) {
                continue;
            }
            
            // Skip if aadhaar already exists
            if (request.getAadhaarNumber() != null && !request.getAadhaarNumber().isBlank()) {
                if (candidatesRepository.findByAadhaarNumber(request.getAadhaarNumber()).isPresent()) {
                    continue;
                }
            }
            
            try {
                Candidate candidate = mapRequestToEntity(request);
                candidates.add(candidate);
            } catch (Exception e) {
                // Log and continue with next candidate
                System.err.println("Error creating candidate: " + e.getMessage());
            }
        }
        
        List<Candidate> savedCandidates = candidatesRepository.saveAll(candidates);
        return mapper.toResponseList(savedCandidates);
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
    @Transactional
    public CandidateResponse updateCandidate(Long candidateId, CandidateRequest request, Long updatedBy) {
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
        
        if (request.getCgpa() != null) {
            oldCandidate.setCgpa(request.getCgpa());
        }
        
        if (request.getHistoryOfArrears() != null) {
            oldCandidate.setHistoryOfArrears(request.getHistoryOfArrears());
        }
        
        if (request.getDegree() != null) {
            oldCandidate.setDegree(request.getDegree());
        }
        
        if (request.getDepartment() != null) {
            oldCandidate.setDepartment(request.getDepartment());
        }
        
        if (request.getPassoutYear() != null) {
            oldCandidate.setPassoutYear(request.getPassoutYear());
        }
        
        if (request.getDateOfBirth() != null) {
            oldCandidate.setDateOfBirth(request.getDateOfBirth());
        }
        
        if (request.getAadhaarNumber() != null) {
            oldCandidate.setAadhaarNumber(request.getAadhaarNumber());
        }
        
        if (request.getIsEligible() != null) {
            oldCandidate.setIsEligible(request.getIsEligible());
        }
        
        if (request.getReason() != null) {
            oldCandidate.setReason(request.getReason());
        }
        
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            oldCandidate.setStatus(CandidateStatus.valueOf(request.getStatus()));
        }
        
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
            overrideRequest.setOverrideReason(request.getReason() != null ? request.getReason() : "Candidate update");
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
        
        // Create copy for change detection
        Candidate oldCandidate = createCandidateCopy(candidate);
        
        // Update status
        CandidateStatus oldStatus = candidate.getStatus();
        CandidateStatus newStatus = CandidateStatus.valueOf(request.getStatus());
        candidate.setStatus(newStatus);
        
        if (request.getReason() != null && !request.getReason().isBlank()) {
            candidate.setReason(request.getReason());
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
            overrideRequest.setOverrideReason(request.getReason() != null ? request.getReason() : "Status update");
            overrideRequest.setCreatedBy(request.getUpdatedBy());
            
            try {
                overrideService.logOverride(overrideRequest);
            } catch (Exception e) {
                System.err.println("Error logging status override: " + e.getMessage());
            }
        }
        
        return mapper.toResponse(updatedCandidate);
    }
    
    /**
     * Helper method to map request DTO to entity
     */
    private Candidate mapRequestToEntity(CandidateRequest request) {
        Candidate candidate = new Candidate();
        
        // Set institute if provided
        if (request.getInstituteId() != null) {
            Institute institute = instituteRepository.findById(request.getInstituteId())
                    .orElseThrow(() -> new RuntimeException("Institute not found with ID: " + request.getInstituteId()));
            candidate.setInstitute(institute);
        }
        
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setEmail(request.getEmail());
        candidate.setMobile(request.getMobile());
        candidate.setCgpa(request.getCgpa());
        candidate.setHistoryOfArrears(request.getHistoryOfArrears());
        candidate.setDegree(request.getDegree());
        candidate.setDepartment(request.getDepartment());
        candidate.setPassoutYear(request.getPassoutYear());
        candidate.setDateOfBirth(request.getDateOfBirth());
        candidate.setAadhaarNumber(request.getAadhaarNumber());
        candidate.setIsEligible(request.getIsEligible());
        candidate.setReason(request.getReason());
        
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            candidate.setStatus(CandidateStatus.valueOf(request.getStatus()));
        } else {
            candidate.setStatus(CandidateStatus.APPLIED); // Default status
        }
        
        return candidate;
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
}

