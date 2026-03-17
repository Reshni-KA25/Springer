package com.kanini.springer.entity.Drive;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.kanini.springer.entity.HiringReq.HiringDemand;
import com.kanini.springer.entity.HiringReq.Skill;

/**
 * Junction table mapping HiringDemand to Skills
 * Represents skills required for each hiring demand
 */
@Entity
@Table(name = "requisition_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequisitionSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer demandSkillId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id")
    private HiringDemand demand;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;
}
