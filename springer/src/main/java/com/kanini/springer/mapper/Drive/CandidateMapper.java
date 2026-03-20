package com.kanini.springer.mapper.Drive;

import com.kanini.springer.dto.Drive.CandidateResponse;
import com.kanini.springer.entity.Drive.Candidate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CandidateMapper {
    
    /**
     * Convert Candidate entity to CandidateResponse DTO
     */
    public CandidateResponse toResponse(Candidate candidate) {
        if (candidate == null) {
            return null;
        }
        
        CandidateResponse response = new CandidateResponse();
        response.setCandidateId(candidate.getCandidateId());
        response.setFirstName(candidate.getFirstName());
        response.setLastName(candidate.getLastName());
        response.setEmail(candidate.getEmail());
        response.setMobile(candidate.getMobile());
        response.setCgpa(candidate.getCgpa());
        response.setHistoryOfArrears(candidate.getHistoryOfArrears());
        response.setDegree(candidate.getDegree());
        response.setDepartment(candidate.getDepartment());
        response.setPassoutYear(candidate.getPassoutYear());
        response.setDateOfBirth(candidate.getDateOfBirth());
        response.setAadhaarNumber(candidate.getAadhaarNumber());
        response.setIsEligible(candidate.getIsEligible());
        response.setReason(candidate.getReason());
        response.setCreatedAt(candidate.getCreatedAt());
        response.setUpdatedAt(candidate.getUpdatedAt());
        
        // Map institute details
        if (candidate.getInstitute() != null) {
            response.setInstituteId(candidate.getInstitute().getInstituteId());
            response.setInstituteName(candidate.getInstitute().getInstituteName());
        }
        
        // Map status enum to string
        if (candidate.getStatus() != null) {
            response.setStatus(candidate.getStatus().toString());
        }
        
        return response;
    }
    
    /**
     * Convert list of Candidate entities to list of response DTOs
     */
    public List<CandidateResponse> toResponseList(List<Candidate> candidates) {
        if (candidates == null) {
            return new ArrayList<>();
        }
        
        return candidates.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
