package com.kanini.springer.dto.Hiring;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest {
    
    @NotBlank(message = "Skill name is required")
    private String skillName;
}
