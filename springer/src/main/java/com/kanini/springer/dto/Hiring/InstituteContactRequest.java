package com.kanini.springer.dto.Hiring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstituteContactRequest {
    
    private Long instituteId;
    private String tpoName;
    private String tpoEmail;
    private String tpoMobile;
    private String tpoStatus; // ACTIVE, INACTIVE
    private Boolean isPrimary;
}
