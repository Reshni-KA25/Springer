package com.kanini.springer.dto.Hiring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstituteResponse {
    
    private Long instituteId;
    private String instituteName;
    private String instituteTier;
    private String location;
    private String state;
    private String city;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
