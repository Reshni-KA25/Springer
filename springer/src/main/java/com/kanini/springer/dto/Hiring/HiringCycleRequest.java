package com.kanini.springer.dto.Hiring;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HiringCycleRequest {
    
    @NotNull(message = "Cycle year is required")
    private Integer cycleYear;
    
    @NotBlank(message = "Cycle name is required")
    private String cycleName;
}
