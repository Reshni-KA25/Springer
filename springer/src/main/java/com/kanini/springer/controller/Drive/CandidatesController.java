package com.kanini.springer.controller.Drive;

import com.kanini.springer.dto.Authentication.ApiResponse;
import com.kanini.springer.dto.Drive.CandidateRequest;
import com.kanini.springer.dto.Drive.CandidateResponse;
import com.kanini.springer.dto.Drive.CandidateStatusUpdateRequest;
import com.kanini.springer.service.Drive.ICandidatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidate Management", description = "APIs for managing candidates in the recruitment system")
public class CandidatesController {
    
    private final ICandidatesService candidatesService;
    
    @PostMapping
    @Operation(summary = "Create a new candidate", description = "Creates a single candidate record")
    public ResponseEntity<ApiResponse<CandidateResponse>> createCandidate(@RequestBody CandidateRequest request) {
        CandidateResponse response = candidatesService.createCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Candidate created successfully", response));
    }
    
    @PostMapping("/bulk")
    @Operation(summary = "Bulk create candidates", description = "Creates multiple candidates at once. Skips duplicates based on email and aadhaar.")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> bulkCreateCandidates(
            @RequestBody List<CandidateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("Request body cannot be empty");
        }
        
        List<CandidateResponse> responses = candidatesService.bulkCreateCandidates(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Candidates created successfully", responses));
    }
    
    @GetMapping
    @Operation(summary = "Get all candidates", description = "Retrieves all candidates in the system")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getAllCandidates() {
        List<CandidateResponse> responses = candidatesService.getAllCandidates();
        return ResponseEntity.ok(new ApiResponse<>(true, "Candidates retrieved successfully", responses));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get candidate by ID", description = "Retrieves a specific candidate by their ID")
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidateById(@PathVariable("id") Long candidateId) {
        CandidateResponse response = candidatesService.getCandidateById(candidateId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Candidate retrieved successfully", response));
    }
    
    @GetMapping("/by-institute/{instituteId}")
    @Operation(summary = "Get candidates by institute", description = "Retrieves all candidates from a specific institute")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getCandidatesByInstituteId(
            @PathVariable("instituteId") Long instituteId) {
        List<CandidateResponse> responses = candidatesService.getCandidatesByInstituteId(instituteId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Candidates retrieved successfully", responses));
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update candidate", 
               description = "Partially updates a candidate. Automatically logs changes to manual_override table if updatedBy is provided.")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateCandidate(
            @PathVariable("id") Long candidateId,
            @RequestBody CandidateRequest request,
            @RequestParam(required = false) Long updatedBy) {
        CandidateResponse response = candidatesService.updateCandidate(candidateId, request, updatedBy);
        return ResponseEntity.ok(new ApiResponse<>(true, "Candidate updated successfully", response));
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update candidate status", 
               description = "Updates the candidate status. Logs the status change to manual_override table.")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateCandidateStatus(
            @PathVariable("id") Long candidateId,
            @RequestBody CandidateStatusUpdateRequest request) {
        CandidateResponse response = candidatesService.updateCandidateStatus(candidateId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Candidate status updated successfully", response));
    }
}

