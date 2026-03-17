package com.kanini.springer.entity.enums;

/**
 * Central enum definitions for the Talent Acquisition Management System
 */
public class Enums {

    // Role names in the system
    public enum RoleName {
        TA_HEAD,
        TA_RECRUITER,
        HIRING_MANAGER,
        MEMBERS,
        HR_OPERATIONS,
        TRAINING_COORDINATOR,
        BU_SPOC,
        SYSTEM_ADMIN
    }

    // Hiring cycle status
    public enum CycleStatus {
        OPEN,
        CLOSED
    }

    // Hiring demand approval status
    public enum ApprovalStatus {
        DRAFT,
        SUBMITTED,
        APPROVED,
        REJECTED
    }

    // Institute tier classification
    public enum InstituteTier {
        TIER_1,
        TIER_2,
        TIER_3
    }

    // TPO/Contact status
    public enum ContactStatus {
        ACTIVE,
        INACTIVE
    }
     public enum BusinessUnit {
        DATA_ANALYTICS_AND_AI,
        SERVICENOW,
        PRODUCT_ENGINEERING
    }

    // Candidate overall status
    public enum CandidateStatus {
        APPLIED,
        SHORTLISTED,
        SCHEDULED,
        SELECTED,
        REJECTED,
        OFFERED,
        JOINED,
        DROPPED
    }

    // Drive mode
    public enum DriveMode {
        ON_CAMPUS,
        OFF_CAMPUS
    }

    // Drive status
    public enum DriveStatus {
        PLANNED,
        CONFIRMED,
        IN_PROGRESS,
        COMPLETED,
        CLOSED
    }

    // Application status
    public enum ApplicationStatus {
        IN_DRIVE,
        DROPPED,
        FAILED, 
        PASSED,
        ON_HOLD,
        SELECTED
        
    }

    // Drive panel assignment status
    public enum AssignmentStatus {
        PLANNED,
        DRAFT,
        SELECTED,
        REJECTED
    }

    // Evaluation status
    public enum EvaluationStatus {
        PASS,
        FAIL,
        ABSENT,
        HOLD
    }

    // Document types
    public enum DocumentType {
        RESUME,
        PHOTO,
        ID_PROOF,
        MARKSHEET,
        PROVISIONAL_CERT,
        DEGREE_CERT,
        EXPERIENCE_LETTER,
        RELIEVING_LETTER
    }

    // Document verification status
    public enum VerificationStatus {
        COLLECTED,
        APPROVED,
        REJECTED,
        PENDING
    }

    // Offer letter response
    public enum OfferResponse {
        ACCEPTED,
        DECLINED,
        PENDING,
        EXPIRED
    }

    // Training course status
    public enum CourseStatus {
        PLANNED,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    // Student performance
    public enum Performance {
        GOOD,
        EXCELLENT,
        NEED_LEARNING,
        DROPPED,
        PROJECT_READY
    }

    // Training score status
    public enum ScoreStatus {
        EXCELLENT,
        GOOD,
        AVERAGE,
        BELOW_AVERAGE
    }

    // Audit entity type
    public enum AuditEntityType {
        DEMAND,
        DRIVE,
        DRIVE_APP,
        ROUND_SCORE,
        DOC,
        OFFER,
        TRAINING,
        USER,
        CANDIDATE
    }

    // Audit action
    public enum AuditAction {
        CREATED,
        UPDATED,
        STATUS_CHANGED,
        LOCKED,
        UNLOCKED,
        OVERRIDE,
        UPLOAD,
        APPROVED,
        REJECTED,
        DELETED
    }

    // Manual override entity type
    public enum OverrideEntityType {
        CANDIDATES,
        DRIVES,
        CANDIDATE_EVALUATIONS,
        HIRING_DEMAND,
        DOCUMENT_SUBMISSIONS,
        OFFER_LETTERS,
        TRAINING_COURSES,
        TRAINING_SCORES
    }
}
