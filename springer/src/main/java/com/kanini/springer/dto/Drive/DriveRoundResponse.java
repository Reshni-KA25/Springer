package com.kanini.springer.dto.Drive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for DriveRound data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriveRoundResponse {
    
    private Integer roundId;
    private Long roundConfigId;
    private String roundName;
    private Integer roundNo;
}
