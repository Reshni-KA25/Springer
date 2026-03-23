// Institute Type Definitions
// Maps to backend DTOs in com.kanini.springer.dto.Hiring

export interface InstituteRequest {
  instituteName: string;
  instituteTier: string; // TIER_1, TIER_2, TIER_3
  location: string; // Google location URL
  state: string;
  city: string;
  isActive: boolean;
}

export interface InstituteResponse {
  instituteId: number;
  instituteName: string;
  instituteTier: string;
  location: string;
  state: string;
  city: string;
  isActive: boolean;
  createdAt: string; // ISO-8601 format from LocalDateTime
}

// Nested TPO details interface
export interface TPODetails {
  tpoId: number;
  tpoName: string;
  tpoEmail: string;
  tpoMobile: string;
  tpoStatus: string;
  isPrimary: boolean;
  createdAt: string; // ISO-8601 format from LocalDateTime
}

// Institute with associated TPO contacts
export interface InstituteWithTPOsResponse {
  instituteId: number;
  instituteName: string;
  instituteTier: string;
  location: string;
  state: string;
  city: string;
  isActive: boolean;
  createdAt: string; // ISO-8601 format from LocalDateTime
  tpoDetails: TPODetails[];
}

// Pagination response for institutes with TPOs
export interface PagedInstituteWithTPOsResponse {
  content: InstituteWithTPOsResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}


