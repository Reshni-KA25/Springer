package com.kanini.springer.service.Hiring.impl;

import com.kanini.springer.dto.Hiring.HiringCycleRequest;
import com.kanini.springer.dto.Hiring.HiringCycleResponse;
import com.kanini.springer.entity.HiringReq.HiringCycle;
import com.kanini.springer.entity.enums.Enums.CycleStatus;
import com.kanini.springer.mapper.Hiring.HiringCycleMapper;
import com.kanini.springer.repository.Hiring.HiringCycleRepository;
import com.kanini.springer.service.Hiring.IHiringCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HiringCycleServiceImpl implements IHiringCycleService {
    
    private final HiringCycleRepository cycleRepository;
    private final HiringCycleMapper mapper;
    
    @Override
    public HiringCycleResponse createCycle(HiringCycleRequest request) {
        // Validation: Check if cycle year already exists
        if (cycleRepository.findByCycleYear(request.getCycleYear()).isPresent()) {
            throw new RuntimeException("Hiring cycle for year " + request.getCycleYear() + " already exists");
        }
        
        // Validation: Cycle year should be current year or future
        if (request.getCycleYear() < java.time.Year.now().getValue()) {
            throw new RuntimeException("Cycle year cannot be in the past");
        }
        
        HiringCycle cycle = new HiringCycle();
        cycle.setCycleYear(request.getCycleYear());
        cycle.setCycleName(request.getCycleName());
        cycle.setStatus(CycleStatus.OPEN); // Always set to OPEN on creation
        
        HiringCycle savedCycle = cycleRepository.save(cycle);
        return mapper.toResponse(savedCycle);
    }
    
    @Override
    public HiringCycleResponse getCycleById(Long cycleId) {
        HiringCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + cycleId));
        return mapper.toResponse(cycle);
    }
    
    @Override
    public List<HiringCycleResponse> getAllCycles() {
        return cycleRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HiringCycleResponse> getCyclesByStatus(String status) {
        try {
            CycleStatus cycleStatus = CycleStatus.valueOf(status.toUpperCase());
            return cycleRepository.findByStatus(cycleStatus).stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid cycle status: " + status + ". Valid values are: OPEN, CLOSED");
        }
    }
    
    @Override
    public HiringCycleResponse updateCycle(Long cycleId, HiringCycleRequest request) {
        HiringCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + cycleId));
        
        // Validation: If changing year, check if new year already exists
        if (!cycle.getCycleYear().equals(request.getCycleYear())) {
            if (cycleRepository.findByCycleYear(request.getCycleYear()).isPresent()) {
                throw new RuntimeException("Hiring cycle for year " + request.getCycleYear() + " already exists");
            }
        }
        
        cycle.setCycleYear(request.getCycleYear());
        cycle.setCycleName(request.getCycleName());
        
        HiringCycle updatedCycle = cycleRepository.save(cycle);
        return mapper.toResponse(updatedCycle);
    }
    
    @Override
    
    public void deleteCycle(Long cycleId) {
        HiringCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + cycleId));
        
        // Validation: Cannot delete cycle if it has demands
        if (cycle.getHiringDemands() != null && !cycle.getHiringDemands().isEmpty()) {
            throw new RuntimeException("Cannot delete cycle with existing hiring demands");
        }
        
        cycleRepository.delete(cycle);
    }
    
    @Override
    public HiringCycleResponse toggleCycleStatus(Long cycleId) {
        HiringCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + cycleId));
        
        // Toggle status between OPEN and CLOSED
        if (cycle.getStatus() == CycleStatus.OPEN) {
            cycle.setStatus(CycleStatus.CLOSED);
        } else {
            cycle.setStatus(CycleStatus.OPEN);
        }
        
        HiringCycle updatedCycle = cycleRepository.save(cycle);
        return mapper.toResponse(updatedCycle);
    }
}
