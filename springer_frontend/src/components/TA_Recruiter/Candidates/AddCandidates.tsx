
import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { candidateApi } from "../../../services/drive.api";
import { instituteApi, skillsApi } from "../../../services/hiring.api";
import type { CandidateRequest } from "../../../types/TA_Recruiter/Drive/candidate.types";
import type { InstituteResponse } from "../../../types/TA_Recruiter/Hiring/institute.types";
import type { SkillResponse } from "../../../types/TA_Recruiter/Hiring/skill.types";
import { showToast } from "../../../utils/toast";
import * as XLSX from "xlsx";
import {
  Box,
  Button,
  Card,
  CardContent,
  TextField,
  Typography,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  Autocomplete,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import AddIcon from "@mui/icons-material/Add";
import UploadIcon from "@mui/icons-material/Upload";
import DownloadIcon from "@mui/icons-material/Download";
import EditIcon from "@mui/icons-material/Edit";
import CloseIcon from "@mui/icons-material/Close";
import "../../../css/TA_Recruiter/Candidates/AddCandidates.css";

const AddCandidates: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const cycleId = (location.state as { cycleId?: number; cycleYear?: number; cycleName?: string })?.cycleId || null;
  const cycleYear = (location.state as { cycleId?: number; cycleYear?: number; cycleName?: string })?.cycleYear;
  const cycleName = (location.state as { cycleId?: number; cycleYear?: number; cycleName?: string })?.cycleName;
  const [addDialog, setAddDialog] = useState(false);
  const [bulkData, setBulkData] = useState<CandidateRequest[]>([]);
  const [editIndex, setEditIndex] = useState<number | null>(null);
  const [showErrorOverlay, setShowErrorOverlay] = useState(false);
  const [errorMessages, setErrorMessages] = useState<string[]>([]);
  const [institutes, setInstitutes] = useState<InstituteResponse[]>([]);
  const [skills, setSkills] = useState<SkillResponse[]>([]);
  const [singleForm, setSingleForm] = useState<CandidateRequest>({
    instituteId: 0,
    cycleId: cycleId || 0,
    firstName: "",
    lastName: "",
    email: "",
    mobile: "",
    cgpa: 0,
    historyOfArrears: 0,
    degree: "",
    department: "",
    passoutYear: new Date().getFullYear(),
    dateOfBirth: "",
    aadhaarNumber: "",
    skillIds: [],
  });

  const fetchInstitutes = async () => {
    try {
      const response = await instituteApi.getAllInstitutes();
      if (response.data) {
        setInstitutes(response.data);
      }
    } catch (error) {
      console.error("Error fetching institutes:", error);
    }
  };

  const fetchSkills = async () => {
    try {
      const response = await skillsApi.getAllSkills();
      if (response.data) {
        console.log("Skills API response:", response);
console.log("Skills data:", response.data);
        setSkills(response.data);
      }
    } catch (error) {
      console.error("Error fetching skills:", error);
    }
  };

  useEffect(() => {
    if (!cycleId) {
      showToast("No cycle selected. Please select a cycle first.", "error");
      navigate("/ta-recruiter/candidates");
      return;
    }
    fetchInstitutes();
    fetchSkills();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleAddSingle = async () => {
    // Regex patterns for validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const mobileRegex = /^[0-9]{10}$/;
    const aadhaarRegex = /^[0-9]{12}$/;
    const nameRegex = /^[a-zA-Z\s]+$/;

    // Validate required fields
    if (
      !singleForm.firstName ||
      !singleForm.email ||
      !singleForm.mobile ||
      !singleForm.instituteId ||
      singleForm.cgpa === 0 ||
      !singleForm.passoutYear ||
      !singleForm.degree ||
      !singleForm.department ||
      singleForm.historyOfArrears === undefined ||
      singleForm.historyOfArrears === null ||
      !singleForm.dateOfBirth
    ) {
      showToast("Please fill all required fields", "error");
      return;
    }

    // Validate first name (only letters and spaces)
    if (!nameRegex.test(singleForm.firstName)) {
      showToast("First name should contain only letters", "error");
      return;
    }

    // Validate last name if provided (only letters and spaces)
    if (singleForm.lastName && !nameRegex.test(singleForm.lastName)) {
      showToast("Last name should contain only letters", "error");
      return;
    }

    // Validate email format
    if (!emailRegex.test(singleForm.email)) {
      showToast("Please enter a valid email address", "error");
      return;
    }

    // Validate mobile number (10 digits)
    if (!mobileRegex.test(singleForm.mobile)) {
      showToast("Mobile number must be exactly 10 digits", "error");
      return;
    }

    // Validate aadhaar number if provided (12 digits)
    if (singleForm.aadhaarNumber && !aadhaarRegex.test(singleForm.aadhaarNumber)) {
      showToast("Aadhaar number must be exactly 12 digits", "error");
      return;
    }

    try {
      await candidateApi.createCandidate(singleForm);
      showToast("Candidate added successfully", "success");
      setAddDialog(false);
      setSingleForm({
        instituteId: 0,
        cycleId: cycleId || 0,
        firstName: "",
        lastName: "",
        email: "",
        mobile: "",
        cgpa: 0,
        historyOfArrears: 0,
        degree: "",
        department: "",
        passoutYear: new Date().getFullYear(),
        dateOfBirth: "",
        aadhaarNumber: "",
        skillIds: [],
      });
    } catch (error: unknown) {
      console.error(error);
      const err = error as { message: string; data?: string };
      // Display error data if present, otherwise show message
      const errorMessage = err.data || err.message || "Failed to add candidate";
      showToast(errorMessage, "error");
    }
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const data = new Uint8Array(event.target?.result as ArrayBuffer);
        const workbook = XLSX.read(data, { type: "array" });
        const sheet = workbook.Sheets[workbook.SheetNames[0]];
        const jsonData = XLSX.utils.sheet_to_json(sheet) as Record<string, unknown>[];

        const candidates: CandidateRequest[] = jsonData.map((row) => ({
          instituteId: Number(row["Institute ID"] || row["instituteId"] || 0),
          cycleId: cycleId || 0,
          firstName: (row["First Name"] || row["firstName"] || "") as string,
          lastName: (row["Last Name"] || row["lastName"] || "") as string,
          email: (row["Email"] || row["email"] || "") as string,
          mobile: (row["Mobile"] || row["mobile"] || "") as string,
          cgpa: Number(row["CGPA"] || row["cgpa"] || 0),
          historyOfArrears: Number(row["History of Arrears"] || row["historyOfArrears"] || 0),
          degree: (row["Degree"] || row["degree"] || "") as string,
          department: (row["Department"] || row["department"] || "") as string,
          passoutYear: Number(row["Passout Year"] || row["passoutYear"] || new Date().getFullYear()),
          dateOfBirth: (row["Date of Birth"] || row["dateOfBirth"] || "") as string,
          aadhaarNumber: (row["Aadhaar Number"] || row["aadhaarNumber"] || "") as string,
          skillIds: row["Skill IDs"] 
            ? String(row["Skill IDs"]).split(",").map((id) => Number(id.trim()))
            : [],
        }));

        // Basic validation
        const errors: string[] = [];
        candidates.forEach((cand, idx) => {
          if (!cand.firstName) errors.push(`Row ${idx + 2}: Missing First Name`);
          if (!cand.email) errors.push(`Row ${idx + 2}: Missing Email`);
          if (!cand.mobile) errors.push(`Row ${idx + 2}: Missing Mobile`);
          if (!cand.instituteId || cand.instituteId === 0) errors.push(`Row ${idx + 2}: Missing Institute ID`);
          if (!cand.cgpa || cand.cgpa === 0) errors.push(`Row ${idx + 2}: Missing CGPA`);
          if (!cand.passoutYear) errors.push(`Row ${idx + 2}: Missing Passout Year`);
        });

        if (errors.length > 0) {
          showToast(`Validation errors found. Check data carefully.`, "error");
          setErrorMessages(errors);
          setShowErrorOverlay(true);
        } else {
          setBulkData(candidates);
          showToast(`${candidates.length} candidates loaded from file`, "success");
        }
      } catch (error) {
        console.error(error);
        showToast("Failed to read file. Please check the format.", "error");
      }
    };
    reader.readAsArrayBuffer(file);
    e.target.value = "";
  };

  const handleBulkUpload = async () => {
    if (bulkData.length === 0) {
      showToast("No data to upload", "error");
      return;
    }

    try {
      const response = await candidateApi.bulkCreateCandidates(bulkData);
      console.log("Bulk upload response:", response);

      // Check if there are any errors in the response
      if (response.data.errorMessages && response.data.errorMessages.length > 0) {
        // Show error overlay
        setErrorMessages(response.data.errorMessages);
        setShowErrorOverlay(true);

        // Show notification based on success/failure
        if (response.data.successfulInserts && response.data.successfulInserts.length > 0) {
          showToast(
            `${response.data.successfulInserts.length} candidates uploaded, ${response.data.errorMessages.length} failed`,
            "error"
          );
        } else {
          showToast("All candidates failed validation. See errors.", "error");
        }
      } else {
        showToast(`${response.data.successfulInserts.length} candidates uploaded successfully`, "success");
        setBulkData([]);
      }
    } catch (error: unknown) {
      const err = error as {
        message: string;
        success: boolean;
        data?: { errorMessages?: string[] };
      };

      if (err.data?.errorMessages?.length) {
        setErrorMessages(err.data.errorMessages);
        setShowErrorOverlay(true);
        showToast("Bulk upload failed. See error details.", "error");
      } else {
        showToast(err.message || "Upload failed", "error");
      }
    }
  };

  const handleDownloadFormat = () => {
    const link = document.createElement("a");
    link.href = "/files/candidate_data.xlsx";
    link.download = "candidate_data.xlsx";
    link.click();
  };

  const handleEditCell = (
    index: number,
    field: keyof CandidateRequest,
    value: string | number | number[]
  ) => {
    const updated = [...bulkData];
    updated[index] = { ...updated[index], [field]: value };
    setBulkData(updated);
  };

  const handleSkillChange = (_: unknown, newValue: SkillResponse[]) => {
    setSingleForm({
      ...singleForm,
      skillIds: newValue.map((skill) => skill.skillId),
    });
  };

  const handleRemoveSkill = (skillIdToRemove: number) => {
    setSingleForm({
      ...singleForm,
      skillIds: singleForm.skillIds.filter((id) => id !== skillIdToRemove),
    });
  };

  const getInstituteName = (id: number) => {
    const institute = institutes.find((inst) => inst.instituteId === id);
    return institute ? institute.instituteName : `ID: ${id}`;
  };

  const getSkillNames = (skillIds: number[]) => {
    return skillIds
      .map((id) => {
        const skill = skills.find((s) => s.skillId === id);
        return skill ? skill.skillName : `ID:${id}`;
      })
      .join(", ");
  };

  return (
    <Box className="add-candidates-container">
      {/* Cycle Info Badge */}
      {cycleYear && (
        <Box sx={{ marginBottom: 2 }}>
          <Typography 
            variant="subtitle1" 
            sx={{ 
              color: 'var(--color-primary)', 
              fontWeight: 600,
              backgroundColor: 'color-mix(in srgb, var(--color-primary) 10%, transparent)',
              padding: '8px 16px',
              borderRadius: '8px',
              display: 'inline-block',
              border: '1px solid var(--color-primary)'
            }}
          >
            📅 {cycleName} - {cycleYear}
          </Typography>
        </Box>
      )}
      
      {/* Header */}
      <Box className="add-candidates-header">
        <IconButton onClick={() => navigate("/ta-recruiter/candidates")} className="back-btn">
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4">Add Candidates</Typography>
      </Box>

      {/* Action Buttons */}
      <Card className="add-candidates-action-card">
        <CardContent>
          <Box className="add-candidates-actions-container">
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setAddDialog(true)}
              className="add-candidates-action-btn add-candidates-add-btn"
            >
              Add Candidate
            </Button>

            <Button
              variant="contained"
              component="label"
              startIcon={<UploadIcon />}
              className="add-candidates-action-btn add-candidates-upload-btn"
            >
              Upload Candidates
              <input type="file" hidden accept=".xlsx,.xls" onChange={handleFileUpload} />
            </Button>

            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              onClick={handleDownloadFormat}
              className="add-candidates-action-btn add-candidates-download-btn"
            >
              Download Format
            </Button>
          </Box>
        </CardContent>
      </Card>

      {/* Bulk Data Table */}
      {bulkData.length > 0 && (
        <Card className="add-candidates-bulk-card">
          <CardContent>
            <Box className="add-candidates-bulk-header">
              <Typography variant="h6">Uploaded Data ({bulkData.length} candidates)</Typography>
              <Button
                variant="contained"
                onClick={handleBulkUpload}
                className="add-candidates-bulk-upload-btn"
              >
                Upload to Database
              </Button>
            </Box>

            <TableContainer component={Paper} className="add-candidates-bulk-table">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell className="table-header">First Name</TableCell>
                    <TableCell className="table-header">Last Name</TableCell>
                    <TableCell className="table-header">Email</TableCell>
                    <TableCell className="table-header">Mobile</TableCell>
                    <TableCell className="table-header">Institute</TableCell>
                    <TableCell className="table-header">CGPA</TableCell>
                    <TableCell className="table-header">Passout Year</TableCell>
                    <TableCell className="table-header">Skills</TableCell>
                    <TableCell className="table-header">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {bulkData.map((cand, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        {editIndex === index ? (
                          <TextField
                            size="small"
                            value={cand.firstName}
                            onChange={(e) => handleEditCell(index, "firstName", e.target.value)}
                          />
                        ) : (
                          cand.firstName
                        )}
                      </TableCell>
                      <TableCell>
                        {editIndex === index ? (
                          <TextField
                            size="small"
                            value={cand.lastName}
                            onChange={(e) => handleEditCell(index, "lastName", e.target.value)}
                          />
                        ) : (
                          cand.lastName
                        )}
                      </TableCell>
                      <TableCell>{cand.email}</TableCell>
                      <TableCell>{cand.mobile}</TableCell>
                      <TableCell>{getInstituteName(cand.instituteId)}</TableCell>
                      <TableCell>{cand.cgpa}</TableCell>
                      <TableCell>{cand.passoutYear}</TableCell>
                      <TableCell>{getSkillNames(cand.skillIds)}</TableCell>
                      <TableCell>
                        <IconButton
                          size="small"
                          onClick={() => setEditIndex(editIndex === index ? null : index)}
                        >
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      {/* Add Single Candidate Dialog */}
      <Dialog open={addDialog} onClose={() => setAddDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>Add New Candidate</DialogTitle>
        <DialogContent>
          <Box className="add-candidates-form">
            <Box className="add-candidates-form-row">
              <TextField
                label="First Name"
                fullWidth
                required
                value={singleForm.firstName}
                onChange={(e) => {
                  const value = e.target.value;
                  // Allow only letters and spaces
                  if (value === "" || /^[a-zA-Z\s]*$/.test(value)) {
                    setSingleForm({ ...singleForm, firstName: value });
                  }
                }}
              />
              <TextField
                label="Last Name"
                fullWidth
                value={singleForm.lastName}
                onChange={(e) => {
                  const value = e.target.value;
                  // Allow only letters and spaces
                  if (value === "" || /^[a-zA-Z\s]*$/.test(value)) {
                    setSingleForm({ ...singleForm, lastName: value });
                  }
                }}
              />
            </Box>

            <Box className="add-candidates-form-row">
              <TextField
                label="Email"
                type="email"
                fullWidth
                required
                value={singleForm.email}
                onChange={(e) => setSingleForm({ ...singleForm, email: e.target.value })}
              />
              <TextField
                label="Mobile"
                fullWidth
                required
                value={singleForm.mobile}
                onChange={(e) => {
                  const value = e.target.value;
                  // Allow only numbers and max 10 digits
                  if (value === "" || (/^[0-9]*$/.test(value) && value.length <= 10)) {
                    setSingleForm({ ...singleForm, mobile: value });
                  }
                }}
                inputProps={{ maxLength: 10 }}
              />
            </Box>

            <Box className="add-candidates-form-row">
              <Autocomplete
                fullWidth
                options={institutes}
                getOptionLabel={(option) => option.instituteName}
                value={institutes.find((inst) => inst.instituteId === singleForm.instituteId) || null}
                onChange={(_, newValue) =>
                  setSingleForm({ ...singleForm, instituteId: newValue?.instituteId || 0 })
                }
                renderInput={(params) => (
                  <TextField {...params} label="Institute" placeholder="Search institute..." required />
                )}
              />
            </Box>

            <Box className="add-candidates-form-row">
              <TextField
                label="CGPA"
                type="number"
                fullWidth
                required
                inputProps={{ step: 0.01, min: 0, max: 10 }}
                value={singleForm.cgpa || ""}
                onChange={(e) =>
                  setSingleForm({ ...singleForm, cgpa: parseFloat(e.target.value) || 0 })
                }
              />
              <TextField
                label="History of Arrears"
                type="number"
                fullWidth
                required
                inputProps={{ min: 0 }}
                value={singleForm.historyOfArrears || ""}
                onChange={(e) =>
                  setSingleForm({ ...singleForm, historyOfArrears: parseInt(e.target.value) || 0 })
                }
              />
            </Box>

            <Box className="add-candidates-form-row">
              <FormControl fullWidth required>
                <InputLabel>Degree</InputLabel>
                <Select
                  value={singleForm.degree || ""}
                  label="Degree"
                  onChange={(e) => setSingleForm({ ...singleForm, degree: e.target.value })}
                >
                  <MenuItem value="BE">BE</MenuItem>
                  <MenuItem value="B.Tech">B.Tech</MenuItem>
                  <MenuItem value="ME">ME</MenuItem>
                  <MenuItem value="M.Tech">M.Tech</MenuItem>
                  <MenuItem value="MSc">MSc</MenuItem>
                  <MenuItem value="BSc">BSc</MenuItem>
                </Select>
              </FormControl>
              <FormControl fullWidth required>
                <InputLabel>Department</InputLabel>
                <Select
                  value={singleForm.department || ""}
                  label="Department"
                  onChange={(e) => setSingleForm({ ...singleForm, department: e.target.value })}
                >
                  <MenuItem value="CSE">CSE</MenuItem>
                  <MenuItem value="EEE">EEE</MenuItem>
                  <MenuItem value="IT">IT</MenuItem>
                  <MenuItem value="ECE">ECE</MenuItem>
                  <MenuItem value="AIML">AIML</MenuItem>
                  <MenuItem value="Civil">Civil</MenuItem>
                  <MenuItem value="Mech">Mech</MenuItem>
                  <MenuItem value="Food Tech">Food Tech</MenuItem>
                  <MenuItem value="Agri">Agri</MenuItem>
                </Select>
              </FormControl>
            </Box>

            <Box className="add-candidates-form-row">
              <TextField
                label="Passout Year"
                type="number"
                fullWidth
                required
                inputProps={{ min: 2020, max: 2050 }}
                value={singleForm.passoutYear || ""}
                onChange={(e) =>
                  setSingleForm({ ...singleForm, passoutYear: parseInt(e.target.value) || 0 })
                }
              />
              <TextField
                label="Date of Birth"
                type="date"
                fullWidth
                required
                InputLabelProps={{ shrink: true }}
                value={singleForm.dateOfBirth}
                onChange={(e) => setSingleForm({ ...singleForm, dateOfBirth: e.target.value })}
              />
            </Box>

            <TextField
              label="Aadhaar Number"
              fullWidth
              value={singleForm.aadhaarNumber}
              onChange={(e) => {
                const value = e.target.value;
                // Allow only numbers and max 12 digits
                if (value === "" || (/^[0-9]*$/.test(value) && value.length <= 12)) {
                  setSingleForm({ ...singleForm, aadhaarNumber: value });
                }
              }}
              inputProps={{ maxLength: 12 }}
              helperText="12 digits (optional)"
            />

            <Autocomplete
              multiple
              fullWidth
              options={skills}
              getOptionLabel={(option) => option.skillName}
              value={skills.filter((skill) => singleForm.skillIds.includes(skill.skillId))}
              onChange={handleSkillChange}
              renderInput={(params) => (
                <TextField {...params} label="Skills" placeholder="Search skills..." />
              )}
              renderTags={() => null}
            />

            {singleForm.skillIds.length > 0 && (
              <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1, mt: 1 }}>
                {singleForm.skillIds.map((skillId) => {
                  const skill = skills.find((s) => s.skillId === skillId);
                  return (
                    <Chip
                      key={skillId}
                      label={skill?.skillName || `ID: ${skillId}`}
                      onDelete={() => handleRemoveSkill(skillId)}
                      color="primary"
                      variant="outlined"
                      size="medium"
                    />
                  );
                })}
              </Box>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddDialog(false)}>Cancel</Button>
          <Button onClick={handleAddSingle} variant="contained" className="add-candidates-add-btn">
            Add Candidate
          </Button>
        </DialogActions>
      </Dialog>

      {/* Error Overlay */}
      {showErrorOverlay && (
        <Box className="error-overlay" onClick={() => setShowErrorOverlay(false)}>
          <Box className="error-overlay-content" onClick={(e) => e.stopPropagation()}>
            <Box className="error-overlay-header">
              <Typography variant="h6" className="error-overlay-title">
                Validation Errors ({errorMessages.length})
              </Typography>
              <IconButton onClick={() => setShowErrorOverlay(false)} size="small">
                <CloseIcon />
              </IconButton>
            </Box>
            <Box className="error-overlay-messages">
              {errorMessages.map((error, index) => (
                <Box key={index} className="error-message-item">
                  <Typography className="error-message-number">{index + 1}.</Typography>
                  <Typography className="error-message-text">{error}</Typography>
                </Box>
              ))}
            </Box>
          </Box>
        </Box>
      )}
    </Box>
  );
};

export default AddCandidates;