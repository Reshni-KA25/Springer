package com.kanini.springer.dto.Hiring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkInsertResponse<T> {
    
    private List<T> successfulInserts;
    private List<BulkInsertError> errors;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkInsertError {
        private String identifier; // name or email or ID
        private String errorMessage;
    }
}
