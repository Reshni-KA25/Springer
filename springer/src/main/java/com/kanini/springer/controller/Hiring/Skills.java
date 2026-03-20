package com.kanini.springer.controller.Hiring;

import com.kanini.springer.dto.Hiring.SkillRequest;
import com.kanini.springer.dto.Hiring.SkillResponse;
import com.kanini.springer.service.Hiring.ISkills;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class Skills {
    
    private final ISkills skillService;
    
    /**
     * Create a new skill
     * POST /api/skills
     */
    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        SkillResponse response = skillService.createSkill(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get skill by ID
     * GET /api/skills/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable Long id) {
        SkillResponse response = skillService.getSkillById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all skills
     * GET /api/skills
     */
    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        List<SkillResponse> skills = skillService.getAllSkills();
        return ResponseEntity.ok(skills);
    }
    
    /**
     * Get skill by name
     * GET /api/skills/name/{skillName}
     */
    @GetMapping("/name/{skillName}")
    public ResponseEntity<SkillResponse> getSkillByName(@PathVariable String skillName) {
        SkillResponse response = skillService.getSkillByName(skillName);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update skill
     * PUT /api/skills/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request) {
        SkillResponse response = skillService.updateSkill(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete skill
     * DELETE /api/skills/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok("Skill deleted successfully");
    }
}
