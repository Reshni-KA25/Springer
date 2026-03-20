package com.kanini.springer.service.Drive;

import com.kanini.springer.dto.Drive.CandidateRequest;
import com.kanini.springer.dto.Drive.CandidateResponse;
import com.kanini.springer.dto.Drive.CandidateStatusUpdateRequest;

import java.util.List;

/**
 * Service interface for candidate operations
 */
public interface ICandidatesService {
    
    /**
     * Create a single candidate
     */
    CandidateResponse createCandidate(CandidateRequest request);
    
    /**
     * Bulk create candidates
     */
    List<CandidateResponse> bulkCreateCandidates(List<CandidateRequest> requests);
    
    /**
     * Get all candidates
     */
    List<CandidateResponse> getAllCandidates();
    
    /**
     * Get candidate by ID
     */
    CandidateResponse getCandidateById(Long candidateId);
    
    /**
     * Get all candidates by institute ID
     */
    List<CandidateResponse> getCandidatesByInstituteId(Long instituteId);
    
    /**
     * Update candidate (with manual override logging)
     */
    CandidateResponse updateCandidate(Long candidateId, CandidateRequest request, Long updatedBy);
    
    /**
     * Update candidate status
     */
    CandidateResponse updateCandidateStatus(Long candidateId, CandidateStatusUpdateRequest request);
}

