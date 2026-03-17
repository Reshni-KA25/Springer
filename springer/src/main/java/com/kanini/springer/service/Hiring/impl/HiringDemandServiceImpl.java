package com.kanini.springer.service.Hiring.impl;

import com.kanini.springer.dto.Hiring.HiringDemandRequest;
import com.kanini.springer.dto.Hiring.HiringDemandResponse;
import com.kanini.springer.entity.HiringReq.HiringCycle;
import com.kanini.springer.entity.HiringReq.HiringDemand;
import com.kanini.springer.entity.HiringReq.User;
import com.kanini.springer.entity.enums.Enums.ApprovalStatus;
import com.kanini.springer.entity.enums.Enums.BusinessUnit;
import com.kanini.springer.entity.enums.Enums.CycleStatus;
import com.kanini.springer.mapper.Hiring.HiringDemandMapper;
import com.kanini.springer.repository.Hiring.HiringCycleRepository;
import com.kanini.springer.repository.Hiring.HiringDemandRepository;
import com.kanini.springer.repository.Hiring.UserRepository;
import com.kanini.springer.service.Hiring.IHiringDemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HiringDemandServiceImpl implements IHiringDemandService {
    
    private final HiringDemandRepository demandRepository;
    private final HiringCycleRepository cycleRepository;
    private final UserRepository userRepository;
    private final HiringDemandMapper mapper;
    
    @Override
    @Transactional
    public HiringDemandResponse createDemand(HiringDemandRequest request, Long userId) {
        // Validation: Check if cycle exists
        HiringCycle cycle = cycleRepository.findById(request.getCycleId())
                .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + request.getCycleId()));
        
        // Validation: Check if cycle is OPEN
        if (cycle.getStatus() != CycleStatus.OPEN) {
            throw new RuntimeException("Cannot create demand for a closed hiring cycle");
        }
        
        // Validation: Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Validation: Demand count must be positive
        if (request.getDemandCount() <= 0) {
            throw new RuntimeException("Demand count must be greater than zero");
        }
        
        HiringDemand demand = new HiringDemand();
        demand.setCycle(cycle);
        demand.setBusinessUnit(BusinessUnit.valueOf(request.getBusinessUnit()));
        demand.setDemandCount(request.getDemandCount());
        demand.setCompensationBand(request.getCompensationBand());
        demand.setJobDescription(request.getJobDescription());
        demand.setApprovalStatus(request.getApprovalStatus());
        demand.setCreatedBy(user);
        
        HiringDemand savedDemand = demandRepository.save(demand);
        return mapper.toResponse(savedDemand);
    }
    
    @Override
    public HiringDemandResponse getDemandById(Long demandId) {
        HiringDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Hiring demand not found with ID: " + demandId));
        return mapper.toResponse(demand);
    }
    
    @Override
    public List<HiringDemandResponse> getAllDemands() {
        return demandRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HiringDemandResponse> getDemandsByCycle(Long cycleId) {
        // Validation: Check if cycle exists
        if (!cycleRepository.existsById(cycleId)) {
            throw new RuntimeException("Hiring cycle not found with ID: " + cycleId);
        }
        
        return demandRepository.findByCycleCycleId(cycleId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HiringDemandResponse> getDemandsByStatus(String status) {
        try {
            ApprovalStatus approvalStatus = ApprovalStatus.valueOf(status.toUpperCase());
            return demandRepository.findByApprovalStatus(approvalStatus).stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid approval status: " + status + 
                    ". Valid values are: DRAFT, SUBMITTED, APPROVED, REJECTED");
        }
    }
    
    @Override
    @Transactional
    public HiringDemandResponse updateDemand(Long demandId, HiringDemandRequest request) {
        HiringDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Hiring demand not found with ID: " + demandId));
        
        // Validation: If changing cycle, check if new cycle exists and is OPEN
        if (!demand.getCycle().getCycleId().equals(request.getCycleId())) {
            HiringCycle newCycle = cycleRepository.findById(request.getCycleId())
                    .orElseThrow(() -> new RuntimeException("Hiring cycle not found with ID: " + request.getCycleId()));
            
            if (newCycle.getStatus() != CycleStatus.OPEN) {
                throw new RuntimeException("Cannot move demand to a closed hiring cycle");
            }
            demand.setCycle(newCycle);
        }
        
        // Validation: Demand count must be positive
        if (request.getDemandCount() <= 0) {
            throw new RuntimeException("Demand count must be greater than zero");
        }
        
        demand.setBusinessUnit(BusinessUnit.valueOf(request.getBusinessUnit()));
        demand.setDemandCount(request.getDemandCount());
        demand.setCompensationBand(request.getCompensationBand());
        demand.setJobDescription(request.getJobDescription());
        demand.setApprovalStatus(request.getApprovalStatus());
        
        HiringDemand updatedDemand = demandRepository.save(demand);
        return mapper.toResponse(updatedDemand);
    }
    
    @Override
    @Transactional
    public void deleteDemand(Long demandId) {
        HiringDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Hiring demand not found with ID: " + demandId));
        
        // Validation: Cannot delete if demand is already approved
        if (demand.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new RuntimeException("Cannot delete approved hiring demand");
        }
        
        // Validation: Cannot delete if demand has drives
        if (demand.getDrives() != null && !demand.getDrives().isEmpty()) {
            throw new RuntimeException("Cannot delete demand with existing drives");
        }
        
        demandRepository.delete(demand);
    }
}
