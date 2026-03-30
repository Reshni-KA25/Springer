package com.kanini.springer.dto.Drive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating application(s) - single or bulk
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    
    private Long driveId; // required
    private List<Long> candidateIds; // required - can be single or multiple
    private Long createdBy; // required - userId
}
