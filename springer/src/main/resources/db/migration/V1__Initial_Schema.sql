-- =====================================================================
-- Flyway Migration V1: Initial Database Schema
-- =====================================================================
-- SQLite Version - Compatible with org.hibernate.community.dialect.SQLiteDialect
-- All tables use IF NOT EXISTS for idempotency
-- Note: Foreign key pragma is configured in application.properties
-- =====================================================================

CREATE TABLE IF NOT EXISTS academy_events (
  event_id INTEGER PRIMARY KEY AUTOINCREMENT,
  batch_number INTEGER DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  description TEXT,
  event_date DATE NOT NULL,
  event_time TIME DEFAULT NULL,
  event_type VARCHAR(255) NOT NULL,
  program_id INTEGER DEFAULT NULL,
  title VARCHAR(255) NOT NULL,
  venue VARCHAR(255) DEFAULT NULL,
  created_by INTEGER DEFAULT NULL,
  FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_event_program_id ON academy_events(program_id);
CREATE INDEX IF NOT EXISTS idx_event_date ON academy_events(event_date);
CREATE INDEX IF NOT EXISTS idx_event_created_by ON academy_events(created_by);

CREATE TABLE IF NOT EXISTS applications (
  application_id INTEGER PRIMARY KEY AUTOINCREMENT,
  application_status TEXT CHECK (application_status IN ('ALLOTED','DROPPED','FAILED','IN_DRIVE','SELECTED')) DEFAULT NULL,
  batch_time DATETIME DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  history TEXT,
  registration_code VARCHAR(255) DEFAULT NULL,
  updated_at DATETIME DEFAULT NULL,
  candidate_id INTEGER DEFAULT NULL,
  created_by INTEGER DEFAULT NULL,
  drive_id INTEGER DEFAULT NULL,
  updated_by INTEGER DEFAULT NULL,
  UNIQUE (drive_id, registration_code),
  FOREIGN KEY (created_by) REFERENCES users(user_id),
  FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id),
  FOREIGN KEY (drive_id) REFERENCES drive_schedule(drive_id),
  FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_app_drive_id ON applications(drive_id);
CREATE INDEX IF NOT EXISTS idx_app_candidate_id ON applications(candidate_id);
CREATE INDEX IF NOT EXISTS idx_app_status ON applications(application_status);

CREATE TABLE IF NOT EXISTS audit_trail (
  log_id INTEGER PRIMARY KEY AUTOINCREMENT,
  action TEXT CHECK (action IN ('APPROVED','CREATED','DELETED','LOCKED','OVERRIDE','REJECTED','STATUS_CHANGED','UNLOCKED','UPDATED','UPLOAD')) DEFAULT NULL,
  changes TEXT DEFAULT NULL,
  entity_id INTEGER DEFAULT NULL,
  entity_type TEXT CHECK (entity_type IN ('CANDIDATE','DEMAND','DOC','DRIVE','DRIVE_APP','OFFER','ROUND_SCORE','TRAINING','USER')) DEFAULT NULL,
  update_reason TEXT,
  updated_at DATETIME DEFAULT NULL,
  updated_by INTEGER DEFAULT NULL,
  FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_audit_entity_type ON audit_trail(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_trail(action);
CREATE INDEX IF NOT EXISTS idx_audit_updated_by ON audit_trail(updated_by);

CREATE TABLE IF NOT EXISTS batch_allocations (
  student_id INTEGER PRIMARY KEY AUTOINCREMENT,
  attendance_percentage DECIMAL(5,2) DEFAULT NULL,
  batch_number INTEGER DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  image BLOB,
  is_active INTEGER DEFAULT NULL,
  overall_weighted_score DECIMAL(5,2) DEFAULT NULL,
  performance TEXT CHECK (performance IN ('DROPPED','EXCELLENT','GOOD','NEED_LEARNING','PROJECT_READY')) DEFAULT NULL,
  transferred_from_student_id INTEGER DEFAULT NULL,
  candidate_id INTEGER DEFAULT NULL,
  program_id INTEGER DEFAULT NULL,
  FOREIGN KEY (program_id) REFERENCES training_programs(program_id),
  FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id)
);

CREATE INDEX IF NOT EXISTS idx_batch_program_id ON batch_allocations(program_id);

CREATE TABLE IF NOT EXISTS batch_courses (
  batch_course_id INTEGER PRIMARY KEY AUTOINCREMENT,
  batch_no INTEGER DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  end_date DATETIME DEFAULT NULL,
  start_date DATETIME DEFAULT NULL,
  status TEXT CHECK (status IN ('ACTIVE','CANCELLED','COMPLETED','PLANNED')) DEFAULT NULL,
  conducted_by INTEGER DEFAULT NULL,
  course_id INTEGER DEFAULT NULL,
  program_id INTEGER DEFAULT NULL,
  UNIQUE (program_id, batch_no, course_id),
  FOREIGN KEY (conducted_by) REFERENCES users(user_id),
  FOREIGN KEY (course_id) REFERENCES training_courses(course_id),
  FOREIGN KEY (program_id) REFERENCES training_programs(program_id)
);

CREATE TABLE IF NOT EXISTS batch_schedules (
  batch_schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,
  batch_number INTEGER NOT NULL,
  created_at DATETIME DEFAULT NULL,
  end_date DATE NOT NULL,
  start_date DATE NOT NULL,
  updated_at DATETIME DEFAULT NULL,
  program_id INTEGER NOT NULL,
  UNIQUE (program_id, batch_number),
  FOREIGN KEY (program_id) REFERENCES training_programs(program_id)
);

CREATE TABLE IF NOT EXISTS candidate_registration (
  registration_id INTEGER PRIMARY KEY AUTOINCREMENT,
  aadhaar_no VARCHAR(12) DEFAULT NULL,
  cgpa DECIMAL(4,2) NOT NULL,
  college_name VARCHAR(255) NOT NULL,
  degree VARCHAR(100) DEFAULT NULL,
  department VARCHAR(100) DEFAULT NULL,
  dob DATE DEFAULT NULL,
  email VARCHAR(255) NOT NULL,
  fname VARCHAR(255) NOT NULL,
  graduation_year INTEGER NOT NULL,
  history_of_arrears INTEGER DEFAULT NULL,
  lname VARCHAR(255) DEFAULT NULL,
  phone VARCHAR(20) NOT NULL,
  skills TEXT,
  status TEXT CHECK (status IN ('IMPORTED','PENDING')) NOT NULL,
  submitted_at DATETIME NOT NULL,
  drive_id INTEGER NOT NULL,
  form_id INTEGER NOT NULL,
  institute_id INTEGER DEFAULT NULL,
  application_type VARCHAR(50) DEFAULT NULL,
  CHECK (history_of_arrears >= 0),
  FOREIGN KEY (drive_id) REFERENCES drive_schedule(drive_id),
  FOREIGN KEY (form_id) REFERENCES form(form_id),
  FOREIGN KEY (institute_id) REFERENCES institutes(institute_id)
);

CREATE INDEX IF NOT EXISTS idx_registration_drive_id ON candidate_registration(drive_id);
CREATE INDEX IF NOT EXISTS idx_registration_form_id ON candidate_registration(form_id);
CREATE INDEX IF NOT EXISTS idx_registration_status ON candidate_registration(status);
CREATE INDEX IF NOT EXISTS idx_registration_submitted_at ON candidate_registration(submitted_at);

CREATE TABLE IF NOT EXISTS candidate_skills (
  candidate_skill_id INTEGER PRIMARY KEY AUTOINCREMENT,
  candidate_id INTEGER DEFAULT NULL,
  skill_id INTEGER DEFAULT NULL,
  UNIQUE (candidate_id, skill_id),
  FOREIGN KEY (skill_id) REFERENCES skills(skill_id),
  FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id)
);

CREATE TABLE IF NOT EXISTS candidates (
  candidate_id INTEGER PRIMARY KEY AUTOINCREMENT,
  aadhaar_number VARCHAR(255) DEFAULT NULL,
  application_stage TEXT CHECK (application_stage IN ('ACCEPTED','APPLIED','DROPPED','INVITED','JOINED','NOT_JOINED','OFFERED','OFFER_REJECTED','REJECTED','SCHEDULED','SELECTED','SHORTLISTED')) DEFAULT NULL,
  application_type TEXT CHECK (application_type IN ('PREMIUM','STANDARD')) DEFAULT NULL,
  cgpa DECIMAL(10,2) DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  date_of_birth DATE DEFAULT NULL,
  degree VARCHAR(255) DEFAULT NULL,
  department VARCHAR(255) DEFAULT NULL,
  email VARCHAR(255) NOT NULL,
  first_name VARCHAR(50) NOT NULL,
  history_of_arrears INTEGER DEFAULT NULL,
  is_eligible INTEGER DEFAULT NULL,
  last_name VARCHAR(255) DEFAULT NULL,
  lifecycle_status TEXT CHECK (lifecycle_status IN ('ACTIVE','CLOSED')) DEFAULT NULL,
  mobile VARCHAR(255) NOT NULL,
  passout_year INTEGER DEFAULT NULL,
  reason TEXT,
  status_history TEXT,
  updated_at DATETIME DEFAULT NULL,
  cycle_id INTEGER DEFAULT NULL,
  drive_id INTEGER DEFAULT NULL,
  institute_id INTEGER DEFAULT NULL,
  user_id INTEGER DEFAULT NULL,
  UNIQUE (email),
  UNIQUE (aadhaar_number),
  FOREIGN KEY (cycle_id) REFERENCES hiring_cycles(cycle_id),
  FOREIGN KEY (drive_id) REFERENCES drive_schedule(drive_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (institute_id) REFERENCES institutes(institute_id)
);

CREATE INDEX IF NOT EXISTS idx_candidate_institute_id ON candidates(institute_id);
CREATE INDEX IF NOT EXISTS idx_candidate_cycle_id ON candidates(cycle_id);
CREATE INDEX IF NOT EXISTS idx_candidate_drive_id ON candidates(drive_id);
CREATE INDEX IF NOT EXISTS idx_candidate_application_stage ON candidates(application_stage);
CREATE INDEX IF NOT EXISTS idx_candidate_passout_year ON candidates(passout_year);

CREATE TABLE IF NOT EXISTS candidates_evaluations (
  score_id INTEGER PRIMARY KEY AUTOINCREMENT,
  review TEXT,
  reviewed_at DATETIME DEFAULT NULL,
  score INTEGER DEFAULT NULL,
  section_score TEXT DEFAULT NULL,
  status TEXT CHECK (status IN ('ABSENT','FAIL','HOLD','PASS','PENDING','SKIP')) NOT NULL,
  application_id INTEGER DEFAULT NULL,
  reviewed_by INTEGER DEFAULT NULL,
  round_config_id INTEGER DEFAULT NULL,
  UNIQUE (application_id, round_config_id, reviewed_by),
  FOREIGN KEY (application_id) REFERENCES applications(application_id),
  FOREIGN KEY (reviewed_by) REFERENCES users(user_id),
  FOREIGN KEY (round_config_id) REFERENCES round_templates(round_config_id)
);

CREATE INDEX IF NOT EXISTS idx_eval_application_id ON candidates_evaluations(application_id);
CREATE INDEX IF NOT EXISTS idx_eval_round_config_id ON candidates_evaluations(round_config_id);

CREATE TABLE IF NOT EXISTS document_submissions (
  candidate_document_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME DEFAULT NULL,
  uploaded_file BLOB,
  uploaded_no INTEGER DEFAULT NULL,
  verification_status TEXT CHECK (verification_status IN ('APPROVED','COLLECTED','PENDING','REJECTED')) DEFAULT NULL,
  candidate_id INTEGER DEFAULT NULL,
  cycle_id INTEGER DEFAULT NULL,
  document_type_id INTEGER DEFAULT NULL,
  FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id),
  FOREIGN KEY (cycle_id) REFERENCES hiring_cycles(cycle_id),
  FOREIGN KEY (document_type_id) REFERENCES document_types(document_type_id)
);

CREATE INDEX IF NOT EXISTS idx_doc_candidate_id ON document_submissions(candidate_id);

CREATE TABLE IF NOT EXISTS document_types (
  document_type_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME DEFAULT NULL,
  document_type VARCHAR(255) DEFAULT NULL,
  UNIQUE (document_type)
);

CREATE TABLE IF NOT EXISTS drive_schedule (
  drive_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME DEFAULT NULL,
  description TEXT,
  drive_mode TEXT CHECK (drive_mode IN ('OFF_CAMPUS','ON_CAMPUS')) NOT NULL,
  drive_name VARCHAR(255) DEFAULT NULL,
  eligibility_locked INTEGER DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  location VARCHAR(255) DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  status TEXT CHECK (status IN ('CLOSED','COMPLETED','CONFIRMED','IN_PROGRESS','PLANNED')) NOT NULL,
  updated_at DATETIME DEFAULT NULL,
  created_by INTEGER DEFAULT NULL,
  cycle_id INTEGER DEFAULT NULL,
  institute_id INTEGER DEFAULT NULL,
  updated_by INTEGER DEFAULT NULL,
  FOREIGN KEY (updated_by) REFERENCES users(user_id),
  FOREIGN KEY (cycle_id) REFERENCES hiring_cycles(cycle_id),
  FOREIGN KEY (institute_id) REFERENCES institutes(institute_id),
  FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_drive_cycle_id ON drive_schedule(cycle_id);
CREATE INDEX IF NOT EXISTS idx_drive_institute_id ON drive_schedule(institute_id);

CREATE TABLE IF NOT EXISTS drivepanel_assignments (
  assignment_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME DEFAULT NULL,
  is_active INTEGER DEFAULT NULL,
  status TEXT CHECK (status IN ('CANCELLED','DRAFT','HOLD','PLANNED','REJECTED','SELECTED')) DEFAULT NULL,
  updated_at DATETIME DEFAULT NULL,
  application_id INTEGER DEFAULT NULL,
  created_by INTEGER DEFAULT NULL,
  drive_id INTEGER DEFAULT NULL,
  round_config_id INTEGER DEFAULT NULL,
  updated_by INTEGER DEFAULT NULL,
  user_id INTEGER DEFAULT NULL,
  UNIQUE (application_id, round_config_id, user_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (updated_by) REFERENCES users(user_id),
  FOREIGN KEY (application_id) REFERENCES applications(application_id),
  FOREIGN KEY (created_by) REFERENCES users(user_id),
  FOREIGN KEY (drive_id) REFERENCES drive_schedule(drive_id),
  FOREIGN KEY (round_config_id) REFERENCES round_templates(round_config_id)
);

CREATE INDEX IF NOT EXISTS idx_assignment_drive_id ON drivepanel_assignments(drive_id);
CREATE INDEX IF NOT EXISTS idx_assignment_application_id ON drivepanel_assignments(application_id);
CREATE INDEX IF NOT EXISTS idx_assignment_round_config_id ON drivepanel_assignments(round_config_id);

CREATE TABLE IF NOT EXISTS email_templates (
  template_id INTEGER PRIMARY KEY AUTOINCREMENT,
  body TEXT,
  subject TEXT,
  template_name TEXT
);

CREATE TABLE IF NOT EXISTS form (
  form_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME NOT NULL,
  form_name VARCHAR(255) NOT NULL,
  status INTEGER NOT NULL,
  updated_at DATETIME DEFAULT NULL,
  drive_id INTEGER NOT NULL,
  FOREIGN KEY (drive_id) REFERENCES drive_schedule(drive_id)
);

CREATE INDEX IF NOT EXISTS idx_form_drive_id ON form(drive_id);
CREATE INDEX IF NOT EXISTS idx_form_status ON form(status);

CREATE TABLE IF NOT EXISTS hiring_cycles (
  cycle_id INTEGER PRIMARY KEY AUTOINCREMENT,
  budget INTEGER DEFAULT NULL,
  compensation_band INTEGER DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  cycle_name VARCHAR(255) DEFAULT NULL,
  cycle_year INTEGER DEFAULT NULL,
  jd BLOB,
  status TEXT CHECK (status IN ('CLOSED','OPEN')) DEFAULT NULL,
  total_intake INTEGER DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_cycle_year ON hiring_cycles(cycle_year);

CREATE TABLE IF NOT EXISTS hiring_demand (
  demand_id INTEGER PRIMARY KEY AUTOINCREMENT,
  approval_status TEXT CHECK (approval_status IN ('APPROVED','DRAFT','REJECTED','SUBMITTED')) DEFAULT NULL,
  business_unit TEXT CHECK (business_unit IN ('DATA_ANALYTICS_AND_AI','PRODUCT_ENGINEERING','SERVICENOW')) DEFAULT NULL,
  compensation_band VARCHAR(255) DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  demand_count INTEGER DEFAULT NULL,
  job_description TEXT,
  updated_at DATETIME DEFAULT NULL,
  created_by INTEGER DEFAULT NULL,
  cycle_id INTEGER DEFAULT NULL,
  FOREIGN KEY (created_by) REFERENCES users(user_id),
  FOREIGN KEY (cycle_id) REFERENCES hiring_cycles(cycle_id)
);

CREATE INDEX IF NOT EXISTS idx_demand_business_unit ON hiring_demand(business_unit);

CREATE TABLE IF NOT EXISTS institute_contacts (
  tpo_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME DEFAULT NULL,
  is_primary INTEGER DEFAULT NULL,
  tpo_designation VARCHAR(255) DEFAULT NULL,
  tpo_email VARCHAR(255) NOT NULL,
  tpo_mobile VARCHAR(255) NOT NULL,
  tpo_name VARCHAR(100) NOT NULL,
  tpo_status TEXT CHECK (tpo_status IN ('ACTIVE','INACTIVE')) DEFAULT NULL,
  updated_at DATETIME DEFAULT NULL,
  institute_id INTEGER DEFAULT NULL,
  FOREIGN KEY (institute_id) REFERENCES institutes(institute_id)
);

CREATE INDEX IF NOT EXISTS idx_contact_institute_id ON institute_contacts(institute_id);
CREATE INDEX IF NOT EXISTS idx_contact_status ON institute_contacts(tpo_status);

CREATE TABLE IF NOT EXISTS institute_programs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  institute_id INTEGER NOT NULL,
  program_id INTEGER NOT NULL,
  UNIQUE (institute_id, program_id),
  FOREIGN KEY (program_id) REFERENCES programs(program_id),
  FOREIGN KEY (institute_id) REFERENCES institutes(institute_id)
);

CREATE INDEX IF NOT EXISTS idx_institute_program_institute ON institute_programs(institute_id);

CREATE TABLE IF NOT EXISTS institutes (
  institute_id INTEGER PRIMARY KEY AUTOINCREMENT,
  city VARCHAR(255) DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  institute_name VARCHAR(200) NOT NULL,
  institute_tier TEXT CHECK (institute_tier IN ('TIER_1','TIER_2','TIER_3')) DEFAULT NULL,
  is_active INTEGER DEFAULT NULL,
  state VARCHAR(255) DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_institute_tier ON institutes(institute_tier);
CREATE INDEX IF NOT EXISTS idx_institute_location ON institutes(state, city);

CREATE TABLE IF NOT EXISTS intern_certificates (
  certificate_id INTEGER PRIMARY KEY AUTOINCREMENT,
  certificate_name VARCHAR(255) NOT NULL,
  file_data BLOB,
  issue_date DATE DEFAULT NULL,
  issuer VARCHAR(255) NOT NULL,
  uploaded_at DATETIME DEFAULT NULL,
  student_id INTEGER NOT NULL,
  FOREIGN KEY (student_id) REFERENCES batch_allocations(student_id)
);

CREATE INDEX IF NOT EXISTS idx_cert_student_id ON intern_certificates(student_id);

CREATE TABLE IF NOT EXISTS intern_profiles (
  profile_id INTEGER PRIMARY KEY AUTOINCREMENT,
  bio TEXT,
  profile_links TEXT DEFAULT NULL,
  updated_at DATETIME DEFAULT NULL,
  user_id INTEGER NOT NULL,
  UNIQUE (user_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS intern_warnings (
  warning_id INTEGER PRIMARY KEY AUTOINCREMENT,
  acknowledged_at DATETIME DEFAULT NULL,
  acknowledgement_comment TEXT,
  course_id INTEGER DEFAULT NULL,
  issued_at DATETIME DEFAULT NULL,
  message TEXT NOT NULL,
  severity TEXT CHECK (severity IN ('MINOR','MODERATE','SEVERE')) NOT NULL,
  status TEXT CHECK (status IN ('ACKNOWLEDGED','ACTIVE')) NOT NULL,
  warning_type TEXT CHECK (warning_type IN ('ATTENDANCE','BEHAVIOUR','OTHER','PERFORMANCE','PUNCTUALITY')) NOT NULL,
  issued_by INTEGER NOT NULL,
  student_id INTEGER NOT NULL,
  FOREIGN KEY (issued_by) REFERENCES users(user_id),
  FOREIGN KEY (student_id) REFERENCES batch_allocations(student_id)
);

CREATE INDEX IF NOT EXISTS idx_warning_student_id ON intern_warnings(student_id);
CREATE INDEX IF NOT EXISTS idx_warning_status ON intern_warnings(status);

CREATE TABLE IF NOT EXISTS leave_requests (
  leave_id INTEGER PRIMARY KEY AUTOINCREMENT,
  applied_at DATETIME DEFAULT NULL,
  from_date DATE NOT NULL,
  leave_type TEXT CHECK (leave_type IN ('EMERGENCY','OTHER','PERSONAL','SICK')) NOT NULL,
  reason TEXT NOT NULL,
  remarks TEXT,
  reviewed_at DATETIME DEFAULT NULL,
  status TEXT CHECK (status IN ('APPROVED','PENDING','REJECTED')) NOT NULL,
  to_date DATE NOT NULL,
  reviewed_by INTEGER DEFAULT NULL,
  student_id INTEGER NOT NULL,
  FOREIGN KEY (student_id) REFERENCES batch_allocations(student_id),
  FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_leave_student_id ON leave_requests(student_id);
CREATE INDEX IF NOT EXISTS idx_leave_status ON leave_requests(status);

CREATE TABLE IF NOT EXISTS manual_override (
  override_id INTEGER PRIMARY KEY AUTOINCREMENT,
  changes TEXT NOT NULL,
  created_at DATETIME NOT NULL,
  entity_id INTEGER NOT NULL,
  entity_type TEXT CHECK (entity_type IN ('APPLICATIONS','CANDIDATES','CANDIDATE_EVALUATIONS','DOCUMENT_SUBMISSIONS','DRIVES','HIRING_DEMAND','OFFER_LETTERS','TRAINING_COURSES','TRAINING_SCORES')) NOT NULL,
  override_reason TEXT NOT NULL,
  created_by INTEGER NOT NULL,
  FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_override_entity ON manual_override(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_override_created_by ON manual_override(created_by);
CREATE INDEX IF NOT EXISTS idx_override_created_at ON manual_override(created_at);

CREATE TABLE IF NOT EXISTS notifications (
  notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME DEFAULT NULL,
  is_read INTEGER DEFAULT NULL,
  message TEXT,
  type VARCHAR(255) DEFAULT NULL,
  sent_by INTEGER DEFAULT NULL,
  sent_to INTEGER DEFAULT NULL,
  FOREIGN KEY (sent_to) REFERENCES users(user_id),
  FOREIGN KEY (sent_by) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_notification_sent_to ON notifications(sent_to);

CREATE TABLE IF NOT EXISTS offer_letters (
  offer_id INTEGER PRIMARY KEY AUTOINCREMENT,
  comment TEXT,
  issue_date DATE DEFAULT NULL,
  responded_date DATE DEFAULT NULL,
  response TEXT CHECK (response IN ('ACCEPTED','DECLINED','EXPIRED','PENDING')) DEFAULT NULL,
  candidate_id INTEGER DEFAULT NULL,
  cycle_id INTEGER DEFAULT NULL,
  FOREIGN KEY (cycle_id) REFERENCES hiring_cycles(cycle_id),
  FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id)
);

CREATE INDEX IF NOT EXISTS idx_offer_candidate_id ON offer_letters(candidate_id);
CREATE INDEX IF NOT EXISTS idx_offer_cycle_id ON offer_letters(cycle_id);

CREATE TABLE IF NOT EXISTS programs (
  program_id INTEGER PRIMARY KEY AUTOINCREMENT,
  program_name TEXT CHECK (program_name IN ('BBA','BCA','B_A','B_COM','B_E','B_SC','B_TECH','DIPLOMA','MBA','MCA','M_A','M_COM','M_E','M_SC','M_TECH','PHD')) NOT NULL,
  UNIQUE (program_name)
);

CREATE TABLE IF NOT EXISTS requisition_skills (
  demand_skill_id INTEGER PRIMARY KEY AUTOINCREMENT,
  demand_id INTEGER DEFAULT NULL,
  skill_id INTEGER DEFAULT NULL,
  UNIQUE (demand_id, skill_id),
  FOREIGN KEY (skill_id) REFERENCES skills(skill_id),
  FOREIGN KEY (demand_id) REFERENCES hiring_demand(demand_id)
);

CREATE TABLE IF NOT EXISTS roles (
  role_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME DEFAULT NULL,
  role_name TEXT CHECK (role_name IN ('BU_SPOC','HIRING_MANAGER','HR_OPERATIONS','INTERN','MEMBERS','SYSTEM_ADMIN','TA_HEAD','TA_MANAGER','TRAINING_COORDINATOR')) DEFAULT NULL,
  UNIQUE (role_name)
);

CREATE TABLE IF NOT EXISTS round_templates (
  round_config_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME DEFAULT NULL,
  is_active INTEGER DEFAULT NULL,
  min_score INTEGER DEFAULT NULL,
  outoff_score INTEGER DEFAULT NULL,
  round_name VARCHAR(255) DEFAULT NULL,
  round_no INTEGER DEFAULT NULL,
  sections TEXT DEFAULT NULL,
  weightage INTEGER DEFAULT NULL,
  created_by INTEGER DEFAULT NULL,
  FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_round_template_active ON round_templates(is_active);

CREATE TABLE IF NOT EXISTS skills (
  skill_id INTEGER PRIMARY KEY AUTOINCREMENT,
  category TEXT CHECK (category IN ('SOFT_SKILL','TECHNICAL')) DEFAULT NULL,
  skill_name VARCHAR(255) DEFAULT NULL,
  UNIQUE (skill_name)
);

CREATE TABLE IF NOT EXISTS training_courses (
  course_id INTEGER PRIMARY KEY AUTOINCREMENT,
  communication_template TEXT DEFAULT NULL,
  course_name VARCHAR(255) DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  description TEXT,
  is_communication INTEGER NOT NULL DEFAULT 0,
  min_score INTEGER DEFAULT NULL,
  weightage INTEGER DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_course_name ON training_courses(course_name);

CREATE TABLE IF NOT EXISTS training_day_attendance (
  attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
  attendance_date DATE NOT NULL,
  is_present INTEGER NOT NULL,
  student_id INTEGER NOT NULL,
  UNIQUE (student_id, attendance_date),
  FOREIGN KEY (student_id) REFERENCES batch_allocations(student_id)
);

CREATE TABLE IF NOT EXISTS training_programs (
  program_id INTEGER PRIMARY KEY AUTOINCREMENT,
  capacity INTEGER DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  location TEXT CHECK (location IN ('BANGALORE','CHENNAI','COIMBATORE','DELHI','HYDERABAD','MUMBAI','PUNE','REMOTE')) DEFAULT NULL,
  number_of_batches INTEGER DEFAULT NULL,
  program_name VARCHAR(255) DEFAULT NULL,
  program_year INTEGER DEFAULT NULL,
  status INTEGER NOT NULL,
  cycle_id INTEGER DEFAULT NULL,
  FOREIGN KEY (cycle_id) REFERENCES hiring_cycles(cycle_id)
);

CREATE INDEX IF NOT EXISTS idx_program_location ON training_programs(location);

CREATE TABLE IF NOT EXISTS training_scores (
  score_id INTEGER PRIMARY KEY AUTOINCREMENT,
  communication_breakdown TEXT DEFAULT NULL,
  created_at DATETIME DEFAULT NULL,
  review TEXT,
  score INTEGER DEFAULT NULL,
  status TEXT CHECK (status IN ('AVERAGE','BELOW_AVERAGE','EXCELLENT','GOOD')) DEFAULT NULL,
  course_id INTEGER DEFAULT NULL,
  reviewed_by INTEGER DEFAULT NULL,
  student_id INTEGER DEFAULT NULL,
  UNIQUE (student_id, course_id),
  FOREIGN KEY (reviewed_by) REFERENCES users(user_id),
  FOREIGN KEY (student_id) REFERENCES batch_allocations(student_id),
  FOREIGN KEY (course_id) REFERENCES training_courses(course_id)
);

CREATE INDEX IF NOT EXISTS idx_training_score_course_id ON training_scores(course_id);

CREATE TABLE IF NOT EXISTS users (
  user_id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at DATETIME DEFAULT NULL,
  department VARCHAR(255) DEFAULT NULL,
  email VARCHAR(255) NOT NULL,
  is_active INTEGER DEFAULT NULL,
  location VARCHAR(255) DEFAULT NULL,
  password VARCHAR(255) NOT NULL,
  username VARCHAR(50) NOT NULL,
  role_id INTEGER DEFAULT NULL,
  FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE INDEX IF NOT EXISTS idx_user_role_id ON users(role_id);

-- =====================================================================
-- END OF INITIAL SCHEMA
-- =====================================================================
