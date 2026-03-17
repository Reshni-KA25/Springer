package com.kanini.springer.controller.Hiring;

import com.kanini.springer.dto.Authentication.ApiResponse;
import com.kanini.springer.dto.Hiring.HiringDemandRequest;
import com.kanini.springer.dto.Hiring.HiringDemandResponse;
import com.kanini.springer.service.Hiring.IHiringDemandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hiring/demands")
@RequiredArgsConstructor
public class HiringDemandController {
    
    private final IHiringDemandService demandService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<HiringDemandResponse>> createDemand(
            @Valid @RequestBody HiringDemandRequest request,
            @RequestParam Long userId) {
        HiringDemandResponse response = demandService.createDemand(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hiring demand created successfully", response));
    }
    
    @GetMapping("/{demandId}")
    public ResponseEntity<ApiResponse<HiringDemandResponse>> getDemandById(@PathVariable Long demandId) {
        HiringDemandResponse response = demandService.getDemandById(demandId);
        return ResponseEntity.ok(ApiResponse.success("Hiring demand retrieved successfully", response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<HiringDemandResponse>>> getAllDemands(
            @RequestParam(required = false) Long cycleId,
            @RequestParam(required = false) String status) {
        List<HiringDemandResponse> responses;
        
        if (cycleId != null) {
            responses = demandService.getDemandsByCycle(cycleId);
        } else if (status != null && !status.isEmpty()) {
            responses = demandService.getDemandsByStatus(status);
        } else {
            responses = demandService.getAllDemands();
        }
        
        return ResponseEntity.ok(ApiResponse.success("Hiring demands retrieved successfully", responses));
    }
    
    @PutMapping("/{demandId}")
    public ResponseEntity<ApiResponse<HiringDemandResponse>> updateDemand(
            @PathVariable Long demandId,
            @Valid @RequestBody HiringDemandRequest request) {
        HiringDemandResponse response = demandService.updateDemand(demandId, request);
        return ResponseEntity.ok(ApiResponse.success("Hiring demand updated successfully", response));
    }
    
    @DeleteMapping("/{demandId}")
    public ResponseEntity<ApiResponse<Void>> deleteDemand(@PathVariable Long demandId) {
        demandService.deleteDemand(demandId);
        return ResponseEntity.ok(ApiResponse.success("Hiring demand deleted successfully", null));
    }
}
