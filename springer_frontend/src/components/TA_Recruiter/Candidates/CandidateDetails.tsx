import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { candidateApi } from "../../../services/drive.api";
import type { CandidateResponse, CandidateUpdateRequest } from "../../../types/TA_Recruiter/Drive/candidate.types";
import { showToast } from "../../../utils/toast";
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  TextField,
  Typography,
  Grid,
  Chip,
  Switch,
  FormControlLabel,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import EditIcon from "@mui/icons-material/Edit";
import SaveIcon from "@mui/icons-material/Save";
import CancelIcon from "@mui/icons-material/Cancel";
import PersonIcon from "@mui/icons-material/Person";
import SchoolIcon from "@mui/icons-material/School";
import EmailIcon from "@mui/icons-material/Email";
import PhoneIcon from "@mui/icons-material/Phone";
import CalendarTodayIcon from "@mui/icons-material/CalendarToday";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import "../../../css/TA_Recruiter/Candidates/CandidateDetails.css";

const CandidateDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [candidate, setCandidate] = useState<CandidateResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [editMode, setEditMode] = useState<boolean>(false);
  const [saving, setSaving] = useState<boolean>(false);
  const [statusUpdateMode, setStatusUpdateMode] = useState<boolean>(false);
  const [selectedStatus, setSelectedStatus] = useState<string>("");
  const [updatingStatus, setUpdatingStatus] = useState<boolean>(false);
  const [editForm, setEditForm] = useState<CandidateUpdateRequest>({
    firstName: "",
    lastName: "",
    email: "",
    mobile: "",
    cgpa: 0,
    historyOfArrears: 0,
    degree: "",
    department: "",
    passoutYear: 0,
    dateOfBirth: "",
    aadhaarNumber: "",
    isEligible: false,
    reason: "",
  });

  const fetchCandidateDetails = useCallback(async (candidateId: number) => {
    setLoading(true);
    try {
      const response = await candidateApi.getCandidateById(candidateId);
      if (response.data) {
        setCandidate(response.data);
        populateEditForm(response.data);
      }
    } catch (error) {
      showToast("Failed to fetch candidate details", "error");
      console.error("Error fetching candidate:", error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (id) {
      fetchCandidateDetails(Number(id));
    }
  }, [id, fetchCandidateDetails]);

  const populateEditForm = (data: CandidateResponse) => {
    setEditForm({
      firstName: data.firstName,
      lastName: data.lastName || "",
      email: data.email,
      mobile: data.mobile,
      cgpa: data.cgpa,
      historyOfArrears: data.historyOfArrears,
      degree: data.degree || "",
      department: data.department || "",
      passoutYear: data.passoutYear,
      dateOfBirth: data.dateOfBirth || "",
      aadhaarNumber: data.aadhaarNumber || "",
      isEligible: data.isEligible,
      reason: data.reason || "",
    });
  };

  const handleEditToggle = () => {
    if (editMode && candidate) {
      // Cancel edit - restore original data
      populateEditForm(candidate);
    }
    setEditMode(!editMode);
  };

  const handleSave = async () => {
    if (!id) return;
    setSaving(true);
    try {
      await candidateApi.updateCandidate(Number(id), editForm);
      showToast("Candidate updated successfully", "success");
      setEditMode(false);
      // Refresh candidate data
      await fetchCandidateDetails(Number(id));
    } catch (error) {
      showToast("Failed to update candidate", "error");
      console.error("Error updating candidate:", error);
    } finally {
      setSaving(false);
    }
  };

  const handleBack = () => {
    navigate("/ta-recruiter/candidates");
  };

  const handleStatusUpdate = async () => {
    if (!id || !selectedStatus) return;
    setUpdatingStatus(true);
    try {
      await candidateApi.updateCandidateStatus(Number(id), {
        status: selectedStatus,
        updatedBy: 1, // TODO: Replace with actual logged-in user ID
      });
      showToast("Candidate status updated successfully", "success");
      setStatusUpdateMode(false);
      setSelectedStatus("");
      // Refresh candidate data
      await fetchCandidateDetails(Number(id));
    } catch (error) {
      showToast("Failed to update candidate status", "error");
      console.error("Error updating status:", error);
    } finally {
      setUpdatingStatus(false);
    }
  };

  const handleStatusModeToggle = () => {
    if (statusUpdateMode) {
      setSelectedStatus("");
    }
    setStatusUpdateMode(!statusUpdateMode);
  };

  if (loading) {
    return (
      <Box className="candidate-details-loading">
        <CircularProgress />
        <Typography>Loading candidate details...</Typography>
      </Box>
    );
  }

  if (!candidate) {
    return (
      <Box className="candidate-details-error">
        <Typography variant="h6">Candidate not found</Typography>
        <Button onClick={handleBack} startIcon={<ArrowBackIcon />}>
          Back to List
        </Button>
      </Box>
    );
  }

  return (
    <Box className="candidate-details-container">
      {/* Header */}
      <Card className="candidate-details-header">
        <Box className="header-content">
          <Box>
            <Button
              startIcon={<ArrowBackIcon />}
              onClick={handleBack}
              className="back-btn"
            >
              Back to Candidates
            </Button>
            <Typography variant="h4" className="candidate-details-title">
              <PersonIcon className="title-icon" />
              Candidate Details
            </Typography>
          </Box>
          <Box className="header-actions">
            {editMode ? (
              <>
                <Button
                  variant="outlined"
                  startIcon={<CancelIcon />}
                  onClick={handleEditToggle}
                  className="cancel-btn"
                  disabled={saving}
                >
                  Cancel
                </Button>
                <Button
                  variant="contained"
                  startIcon={<SaveIcon />}
                  onClick={handleSave}
                  className="save-btn"
                  disabled={saving}
                >
                  {saving ? "Saving..." : "Save Changes"}
                </Button>
              </>
            ) : (
              <Button
                variant="contained"
                startIcon={<EditIcon />}
                onClick={handleEditToggle}
                className="edit-btn"
              >
                Edit Details
              </Button>
            )}
          </Box>
        </Box>
      </Card>

      {/* Candidate Info Cards */}
      <Grid container spacing={3}>
        {/* Personal Information */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card className="info-card">
            <CardContent>
              <Typography variant="h6" className="section-title">
                <PersonIcon className="section-icon" />
                Personal Information
              </Typography>
              
              <Box className="info-grid">
                <Box className="info-item">
                  <Typography className="info-label">First Name</Typography>
                  {editMode ? (
                    <TextField
                      fullWidth
                      size="small"
                      value={editForm.firstName}
                      onChange={(e) =>
                        setEditForm({ ...editForm, firstName: e.target.value })
                      }
                    />
                  ) : (
                    <Typography className="info-value">{candidate.firstName}</Typography>
                  )}
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">Last Name</Typography>
                  {editMode ? (
                    <TextField
                      fullWidth
                      size="small"
                      value={editForm.lastName}
                      onChange={(e) =>
                        setEditForm({ ...editForm, lastName: e.target.value })
                      }
                    />
                  ) : (
                    <Typography className="info-value">
                      {candidate.lastName || "N/A"}
                    </Typography>
                  )}
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">
                    <EmailIcon className="info-icon" /> Email
                  </Typography>
                  {editMode ? (
                    <TextField
                      fullWidth
                      size="small"
                      type="email"
                      value={editForm.email}
                      onChange={(e) =>
                        setEditForm({ ...editForm, email: e.target.value })
                      }
                    />
                  ) : (
                    <Typography className="info-value">{candidate.email}</Typography>
                  )}
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">
                    <PhoneIcon className="info-icon" /> Mobile
                  </Typography>
                  {editMode ? (
                    <TextField
                      fullWidth
                      size="small"
                      value={editForm.mobile}
                      onChange={(e) =>
                        setEditForm({ ...editForm, mobile: e.target.value })
                      }
                    />
                  ) : (
                    <Typography className="info-value">{candidate.mobile}</Typography>
                  )}
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">
                    <CalendarTodayIcon className="info-icon" /> Date of Birth
                  </Typography>
                  {editMode ? (
                    <TextField
                      fullWidth
                      size="small"
                      type="date"
                      value={editForm.dateOfBirth}
                      onChange={(e) =>
                        setEditForm({ ...editForm, dateOfBirth: e.target.value })
                      }
                    />
                  ) : (
                    <Typography className="info-value">
                      {candidate.dateOfBirth || "N/A"}
                    </Typography>
                  )}
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">Aadhaar Number</Typography>
                  {editMode ? (
                    <TextField
                      fullWidth
                      size="small"
                      value={editForm.aadhaarNumber}
                      onChange={(e) =>
                        setEditForm({ ...editForm, aadhaarNumber: e.target.value })
                      }
                    />
                  ) : (
                    <Typography className="info-value">
                      {candidate.aadhaarNumber || "N/A"}
                    </Typography>
                  )}
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Academic Information */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card className="info-card">
            <CardContent>
              <Typography variant="h6" className="section-title">
                <SchoolIcon className="section-icon" />
                Academic Information
              </Typography>
              
              <Box className="info-grid">
                <Box className="info-item">
                  <Typography className="info-label">
                    <SchoolIcon className="info-icon" /> Institute
                  </Typography>
                  <Typography className="info-value">
                    {candidate.instituteName || "N/A"}
                  </Typography>
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">
                    <LocationOnIcon className="info-icon" /> Location
                  </Typography>
                  <Typography className="info-value">
                    {candidate.city && candidate.state
                      ? `${candidate.city}, ${candidate.state}`
                      : "N/A"}
                  </Typography>
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">Degree</Typography>
                  <Typography className="info-value">
                    {candidate.degree || "N/A"}
                  </Typography>
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">Department</Typography>
                  <Typography className="info-value">
                    {candidate.department || "N/A"}
                  </Typography>
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">CGPA</Typography>
                  <Typography className="info-value">
                    {candidate.cgpa?.toFixed(2) || "N/A"}
                  </Typography>
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">History of Arrears</Typography>
                  <Typography className="info-value">
                    {candidate.historyOfArrears || 0}
                  </Typography>
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">Passout Year</Typography>
                  <Typography className="info-value">
                    {candidate.passoutYear || "N/A"}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Eligibility & Status */}
        <Grid size={{ xs: 12 }}>
          <Card className="info-card">
            <CardContent>
              <Typography variant="h6" className="section-title">
                Eligibility & Status
              </Typography>
              
              <Box className="info-grid-horizontal">
                <Box className="info-item">
                  <Typography className="info-label">Eligibility</Typography>
                  {editMode ? (
                    <FormControlLabel
                      control={
                        <Switch
                          checked={editForm.isEligible}
                          onChange={(e) =>
                            setEditForm({ ...editForm, isEligible: e.target.checked })
                          }
                        />
                      }
                      label={editForm.isEligible ? "Eligible" : "Not Eligible"}
                    />
                  ) : (
                    <Chip
                      label={candidate.isEligible ? "Eligible" : "Not Eligible"}
                      color={candidate.isEligible ? "success" : "error"}
                      className="eligibility-chip"
                    />
                  )}
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">Status</Typography>
                  {statusUpdateMode ? (
                    <Box className="status-update-container">
                      <FormControl size="small" className="status-select">
                        <InputLabel>Update Status</InputLabel>
                        <Select
                          value={selectedStatus}
                          label="Update Status"
                          onChange={(e) => setSelectedStatus(e.target.value)}
                        >
                          <MenuItem value="APPLIED">APPLIED</MenuItem>
                          <MenuItem value="SHORTLISTED">SHORTLISTED</MenuItem>
                          <MenuItem value="SELECTED">SELECTED</MenuItem>
                          <MenuItem value="REJECTED">REJECTED</MenuItem>
                          <MenuItem value="OFFERED">OFFERED</MenuItem>
                          <MenuItem value="JOINED">JOINED</MenuItem>
                          <MenuItem value="DROPPED">DROPPED</MenuItem>
                        </Select>
                      </FormControl>
                      <Button
                        variant="contained"
                        size="small"
                        onClick={handleStatusUpdate}
                        disabled={!selectedStatus || updatingStatus}
                        className="status-update-btn"
                      >
                        {updatingStatus ? "Updating..." : "Update"}
                      </Button>
                      <Button
                        variant="outlined"
                        size="small"
                        onClick={handleStatusModeToggle}
                        className="status-cancel-btn"
                      >
                        Cancel
                      </Button>
                    </Box>
                  ) : (
                    <Box className="status-display-container">
                      <Chip
                        label={candidate.status}
                        color="primary"
                        className="status-chip"
                      />
                      <Button
                        variant="outlined"
                        size="small"
                        onClick={handleStatusModeToggle}
                        className="change-status-btn"
                      >
                        Change Status
                      </Button>
                    </Box>
                  )}
                </Box>

                <Box className="info-item">
                  <Typography className="info-label">Skills</Typography>
                  <Box className="skills-container">
                    {candidate.skillNames && candidate.skillNames.length > 0 ? (
                      candidate.skillNames.map((skill, index) => (
                        <Chip key={index} label={skill} size="small" className="skill-chip" />
                      ))
                    ) : (
                      <Typography className="info-value">No skills added</Typography>
                    )}
                  </Box>
                </Box>

                <Box className="info-item-full">
                  <Typography className="info-label">Reason / Notes</Typography>
                  <Typography className="info-value reason-text">
                    {candidate.reason || "No additional notes"}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default CandidateDetails;
