package com.kanini.springer.entity.utils;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.kanini.springer.entity.HiringReq.User;
import com.kanini.springer.entity.enums.Enums.OverrideEntityType;

/**
 * Records manual overrides made to entities for audit purposes
 */
@Entity
@Table(name = "manual_override")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualOverride {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long overrideId;
    
    @Enumerated(EnumType.STRING)
    private OverrideEntityType entityType;
    
    private Long entityId; // pk of that particular entity type
    
    @Column(columnDefinition = "TEXT")
    private String overrideReason;
    
    @Column(columnDefinition = "JSON")
    private String oldValue;
    
    @Column(columnDefinition = "JSON")
    private String newValue;
    
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy; // user who made the override
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
