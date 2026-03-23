// Skill Type Definitions
// Maps to backend DTOs in com.kanini.springer.dto.Hiring

export interface SkillRequest {
  skillName: string;
}

export interface SkillResponse {
  skillId: number;
  skillName: string;
}

