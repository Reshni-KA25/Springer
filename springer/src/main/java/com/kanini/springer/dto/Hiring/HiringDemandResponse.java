package com.kanini.springer.dto.Hiring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HiringDemandResponse {
    
    private Long demandId;
    private Long cycleId;
    private String cycleName;
    private String businessUnit;
    private Integer demandCount;
    private String compensationBand;
    private String approvalStatus;
    private String createdByUsername;
    private LocalDateTime createdAt;
}
