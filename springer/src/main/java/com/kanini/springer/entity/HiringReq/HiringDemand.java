package com.kanini.springer.entity.HiringReq;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.kanini.springer.entity.Drive.Drive;
import com.kanini.springer.entity.Drive.RequisitionSkill;
import com.kanini.springer.entity.enums.Enums.ApprovalStatus;
import com.kanini.springer.entity.enums.Enums.BusinessUnit;

/**
 * Demand collected from each practices before hiring
 */
@Entity
@Table(name = "hiring_demand")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HiringDemand {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long demandId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id")
    private HiringCycle cycle;
    
    @Enumerated(EnumType.STRING)
    private BusinessUnit businessUnit; // BU/Practice name
    
    private Integer demandCount; // intake count
    
    private String compensationBand; // predefined bands
    
    @Lob
    private byte[] jobDescription; // JD text or link OR PDF
    
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL)
    private List<RequisitionSkill> requisitionSkills;
    
    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL)
    private List<Drive> drives;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
