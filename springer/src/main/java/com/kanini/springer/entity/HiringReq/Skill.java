package com.kanini.springer.entity.HiringReq;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.kanini.springer.entity.Drive.CandidateSkill;
import com.kanini.springer.entity.Drive.RequisitionSkill;

/**
 * Master table containing all available skills in the system
 * Referenced by both HiringDemand (via RequisitionSkill) and Candidate (via CandidateSkill)
 */
@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skillId;
    
    private String skillName; // e.g., Java, Testing, Python
    
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL)
    private List<RequisitionSkill> requisitionSkills;
    
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL)
    private List<CandidateSkill> candidateSkills;
}
