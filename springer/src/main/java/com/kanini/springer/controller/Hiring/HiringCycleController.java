package com.kanini.springer.controller.Hiring;

import com.kanini.springer.dto.Authentication.ApiResponse;
import com.kanini.springer.dto.Hiring.HiringCycleRequest;
import com.kanini.springer.dto.Hiring.HiringCycleResponse;
import com.kanini.springer.service.Hiring.IHiringCycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hiring/cycles")
@RequiredArgsConstructor
public class HiringCycleController {
    
    private final IHiringCycleService cycleService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<HiringCycleResponse>> createCycle(@Valid @RequestBody HiringCycleRequest request) {
        HiringCycleResponse response = cycleService.createCycle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hiring cycle created successfully", response));
    }
    
    @GetMapping("/{cycleId}")
    public ResponseEntity<ApiResponse<HiringCycleResponse>> getCycleById(@PathVariable Long cycleId) {
        HiringCycleResponse response = cycleService.getCycleById(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Hiring cycle retrieved successfully", response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<HiringCycleResponse>>> getAllCycles(@RequestParam(required = false) String status) {
        List<HiringCycleResponse> responses = (status != null && !status.isEmpty()) 
                ? cycleService.getCyclesByStatus(status)
                : cycleService.getAllCycles();
        return ResponseEntity.ok(ApiResponse.success("Hiring cycles retrieved successfully", responses));
    }
    
    @PutMapping("/{cycleId}")
    public ResponseEntity<ApiResponse<HiringCycleResponse>> updateCycle(
            @PathVariable Long cycleId,
            @Valid @RequestBody HiringCycleRequest request) {
        HiringCycleResponse response = cycleService.updateCycle(cycleId, request);
        return ResponseEntity.ok(ApiResponse.success("Hiring cycle updated successfully", response));
    }
    
    @DeleteMapping("/{cycleId}")
    public ResponseEntity<ApiResponse<Void>> deleteCycle(@PathVariable Long cycleId) {
        cycleService.deleteCycle(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Hiring cycle deleted successfully", null));
    }
    
    @PatchMapping("/{cycleId}/toggle-status")
    public ResponseEntity<ApiResponse<HiringCycleResponse>> toggleCycleStatus(@PathVariable Long cycleId) {
        HiringCycleResponse response = cycleService.toggleCycleStatus(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Cycle status toggled successfully", response));
    }
}
