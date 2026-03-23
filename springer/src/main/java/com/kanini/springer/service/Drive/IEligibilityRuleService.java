package com.kanini.springer.service.Drive;

import com.kanini.springer.dto.Drive.EligibilityRuleUpdateRequest;
import com.kanini.springer.dto.Drive.EligibilityValidationResult;

import java.math.BigDecimal;

public interface IEligibilityRuleService {
    
    /**
     * Check eligibility based on the rules in EligibilityRule.json
     * @param cgpa Candidate's CGPA
     * @param passoutYear Candidate's passout year
     * @param historyOfArrears Number of arrears
     * @return EligibilityValidationResult with eligible status and failure messages
     */
    EligibilityValidationResult checkEligibility(BigDecimal cgpa, Integer passoutYear, Integer historyOfArrears);
    
    /**
     * Get all eligibility rules from JSON file
     */
    EligibilityRuleUpdateRequest getAllRules();
    
    /**
     * Update eligibility rules in JSON file
     */
    EligibilityRuleUpdateRequest updateRules(EligibilityRuleUpdateRequest request);
}
