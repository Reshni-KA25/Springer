package com.kanini.springer.entity.Drive;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * For a particular drive what are the rounds included will be mapped here
 */
@Entity
@Table(name = "drive_rounds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriveRound {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roundId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id")
    private Drive drive;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_config_id")
    private RoundTemplate roundConfig;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
