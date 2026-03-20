package com.kanini.springer.dto.Hiring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstituteRequest {
    
    private String instituteName;
    private String instituteTier; // TIER_1, TIER_2, TIER_3
    private String location; // Google location URL
    private String state;
    private String city;
    private Boolean isActive;
}
