// Application Type Definitions
// Maps to backend DTOs in com.kanini.springer.dto.Drive

export interface ApplicationRequest {
  driveId: number;
  candidateIds: number[];
  createdBy: number;
}

export interface ApplicationResponse {
  applicationId: number;
  driveId: number;
  driveName: string;
  candidateId: number;
  candidateName: string;
  candidateEmail: string;
  registrationCode: string;
  applicationStatus: string;
  createdAt: string; // ISO-8601 format from LocalDateTime
  createdBy: number;
  createdByName: string;
}

export interface ApplicationStatusUpdateRequest {
  applicationStatus: string;
}

export interface BulkApplicationResponse {
  successfulApplications: ApplicationResponse[];
  errorMessages: string[];
  totalProcessed: number;
  successCount: number;
  failureCount: number;
}

// Nested type for bulk application status update
export interface ApplicationStatusData {
  applicationId: number;
  applicationStatus: string;
}

export interface BulkApplicationStatusUpdateRequest {
  applications: ApplicationStatusData[];
  updatedBy: number;
}

export interface BulkApplicationStatusUpdateResponse {
  totalProcessed: number;
  successCount: number;
  failureCount: number;
  successfulUpdates: ApplicationResponse[];
  errorMessages: string[];
}
