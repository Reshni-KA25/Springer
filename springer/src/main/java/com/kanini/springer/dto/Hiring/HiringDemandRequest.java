package com.kanini.springer.dto.Hiring;

import com.kanini.springer.entity.enums.Enums.ApprovalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HiringDemandRequest {
    
    @NotNull(message = "Cycle ID is required")
    private Long cycleId;
    
    @NotBlank(message = "Business unit is required")
    private String businessUnit;
    
    @NotNull(message = "Demand count is required")
    private Integer demandCount;
    
    @NotBlank(message = "Compensation band is required")
    private String compensationBand;
    
    private byte[] jobDescription;
    
    @NotNull(message = "Approval status is required")
    private ApprovalStatus approvalStatus;
}
