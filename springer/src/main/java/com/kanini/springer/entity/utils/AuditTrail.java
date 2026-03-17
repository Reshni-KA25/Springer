package com.kanini.springer.entity.utils;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.kanini.springer.entity.enums.Enums.AuditEntityType;
import com.kanini.springer.entity.HiringReq.User;
import com.kanini.springer.entity.enums.Enums.AuditAction;

/**
 * The track and monitor the audits of each action
 */
@Entity
@Table(name = "audit_trail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;
    
    @Enumerated(EnumType.STRING)
    private AuditEntityType entityType;
    
    private Long entityId;
    
    @Enumerated(EnumType.STRING)
    private AuditAction action;
    
    @Column(columnDefinition = "JSON")
    private String oldValue;
    
    @Column(columnDefinition = "JSON")
    private String newValue;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    @Column(columnDefinition = "TEXT")
    private String updateReason; // required for overrides/unlocks/rejections
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
}
