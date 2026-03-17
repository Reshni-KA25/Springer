package com.kanini.springer.mapper.Hiring;

import com.kanini.springer.dto.Hiring.HiringDemandResponse;
import com.kanini.springer.entity.HiringReq.HiringDemand;
import org.springframework.stereotype.Component;

@Component
public class HiringDemandMapper {
    
    public HiringDemandResponse toResponse(HiringDemand demand) {
        HiringDemandResponse response = new HiringDemandResponse();
        response.setDemandId(demand.getDemandId());
        response.setCycleId(demand.getCycle().getCycleId());
        response.setCycleName(demand.getCycle().getCycleName());
        
        response.setBusinessUnit(demand.getBusinessUnit().toString());
        response.setDemandCount(demand.getDemandCount());
        response.setCompensationBand(demand.getCompensationBand());
        response.setApprovalStatus(demand.getApprovalStatus().toString());
        response.setCreatedByUsername(demand.getCreatedBy().getUsername());
        response.setCreatedAt(demand.getCreatedAt());
        return response;
    }
}
