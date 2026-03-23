// Candidate Type Definitions
// Maps to backend DTOs in com.kanini.springer.dto.Drive

export interface CandidateRequest {
  instituteId: number;
  cycleId?: number;
  firstName: string;
  lastName: string;
  email: string;
  mobile: string;
  cgpa: number; // BigDecimal mapped to number
  historyOfArrears: number;
  degree: string;
  department: string;
  passoutYear: number;
  dateOfBirth: string; // YYYY-MM-DD format from LocalDate
  aadhaarNumber: string;
  skillIds: number[];
}

export interface CandidateResponse {
  candidateId: number;
  instituteId: number;
  instituteName: string;
  state: string;
  city: string;
  cycleId: number;
  firstName: string;
  lastName: string;
  email: string;
  mobile: string;
  cgpa: number;
  historyOfArrears: number;
  degree: string;
  department: string;
  passoutYear: number;
  dateOfBirth: string; // YYYY-MM-DD format
  aadhaarNumber: string;
  isEligible: boolean;
  reason: string;
  status: string;
  createdAt: string; // ISO-8601 format from LocalDateTime
  updatedAt: string; // ISO-8601 format from LocalDateTime
  skillNames: string[];
}

export interface CandidateUpdateRequest {
  instituteId?: number;
  cycleId?: number;
  firstName?: string;
  lastName?: string;
  email?: string;
  mobile?: string;
  cgpa?: number;
  historyOfArrears?: number;
  degree?: string;
  department?: string;
  passoutYear?: number;
  dateOfBirth?: string;
  aadhaarNumber?: string;
  isEligible?: boolean;
  reason: string; // Required field for updates
}

export interface CandidateStatusUpdateRequest {
  status: string;
  updatedBy: number;
}

export interface BulkCandidateStatusUpdateRequest {
  candidateIds: number[];
  status: string;
  reason: string;
  updatedBy: number;
}

export interface BulkCandidateStatusUpdateResponse {
  successfulCandidateIds: number[];
  errorMessages: string[];
  totalProcessed: number;
  successCount: number;
  failureCount: number;
}

export interface BulkCandidateCreateResponse {
  successfulInserts: CandidateResponse[];
  errorMessages: string[];
  totalProcessed: number;
  successCount: number;
  failureCount: number;
}
