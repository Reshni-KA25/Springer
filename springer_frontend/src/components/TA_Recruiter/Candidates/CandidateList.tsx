import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { candidateApi } from "../../../services/drive.api";
import { hiringCycleApi } from "../../../services/hiring.api";
import type { CandidateResponse } from "../../../types/TA_Recruiter/Drive/candidate.types";
import type { HiringCycleSummaryResponse } from "../../../types/TA_Recruiter/Hiring/hiringCycle.types";
import { showToast } from "../../../utils/toast";
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  TextField,
  Typography,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Tooltip,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import SearchIcon from "@mui/icons-material/Search";
import ClearIcon from "@mui/icons-material/Clear";
import PersonIcon from "@mui/icons-material/Person";
import VisibilityIcon from "@mui/icons-material/Visibility";
import SchoolIcon from "@mui/icons-material/School";
import EmailIcon from "@mui/icons-material/Email";
import UpdateIcon from "@mui/icons-material/Update";
import "../../../css/TA_Recruiter/Candidates/CandidateList.css";

interface Filters {
  candidateName: string;
  instituteName: string;
  status: string;
  eligibility: string;
}

const CandidateList: React.FC = () => {
  const navigate = useNavigate();
  const [cycles, setCycles] = useState<HiringCycleSummaryResponse[]>([]);
  const [selectedCycle, setSelectedCycle] = useState<number | null>(null);
  const [allCandidates, setAllCandidates] = useState<CandidateResponse[]>([]);
  const [filteredCandidates, setFilteredCandidates] = useState<CandidateResponse[]>([]);
  const [candidatesLoading, setCandidatesLoading] = useState<boolean>(false);
  const [bulkStatusUpdate, setBulkStatusUpdate] = useState<string>("");
  const [updatingBulkStatus, setUpdatingBulkStatus] = useState<boolean>(false);
  const [filters, setFilters] = useState<Filters>({
    candidateName: "",
    instituteName: "",
    status: "",
    eligibility: "",
  });

  // Extract unique values for filter dropdowns
  const uniqueInstitutes = Array.from(
    new Set(allCandidates.map((cand) => cand.instituteName).filter(Boolean))
  );
  const uniqueStatuses = Array.from(
    new Set(allCandidates.map((cand) => cand.status).filter(Boolean))
  );

  useEffect(() => {
    fetchCycles();
  }, []);

  const fetchCycles = async () => {
    try {
      const response = await hiringCycleApi.getAllCycleSummaries();
      if (response.data) {
        // Sort by year descending (largest year first)
        const sortedCycles = response.data.sort((a, b) => b.cycleYear - a.cycleYear);
        setCycles(sortedCycles);
      }
    } catch (error) {
      showToast("Failed to fetch hiring cycles", "error");
      console.error("Error fetching cycles:", error);
    }
  };

  useEffect(() => {
    if (selectedCycle !== null) {
      fetchCandidatesByCycle(selectedCycle);
    }
  }, [selectedCycle]);

  const fetchCandidatesByCycle = async (cycleId: number) => {
    setCandidatesLoading(true);
    try {
      const response = await candidateApi.getCandidatesByCycleId(cycleId);
      if (response.data) {
        setAllCandidates(response.data);
        setFilteredCandidates(response.data);
      }
    } catch (error) {
      showToast("Failed to fetch candidates", "error");
      console.error("Error fetching candidates:", error);
    } finally {
      setCandidatesLoading(false);
    }
  };

  useEffect(() => {
    let filtered = [...allCandidates];

    if (filters.candidateName) {
      filtered = filtered.filter((candidate) => {
        const fullName = `${candidate.firstName} ${candidate.lastName || ""}`.toLowerCase();
        return fullName.includes(filters.candidateName.toLowerCase());
      });
    }

    if (filters.instituteName) {
      filtered = filtered.filter(
        (candidate) => candidate.instituteName === filters.instituteName
      );
    }

    if (filters.status) {
      filtered = filtered.filter((candidate) => candidate.status === filters.status);
    }

    if (filters.eligibility) {
      const isEligible = filters.eligibility === "eligible";
      filtered = filtered.filter((candidate) => candidate.isEligible === isEligible);
    }

    setFilteredCandidates(filtered);
  }, [filters, allCandidates]);

  const handleFilterChange = (field: keyof Filters, value: string) => {
    setFilters((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const clearFilters = () => {
    setFilters({
      candidateName: "",
      instituteName: "",
      status: "",
      eligibility: "",
    });
  };

  const handleCandidateView = (candidateId: number) => {
    navigate(`/ta-recruiter/candidates/${candidateId}`);
  };

  const handleAddCandidate = () => {
    const selectedCycleData = cycles.find(c => c.cycleId === selectedCycle);
    navigate("/ta-recruiter/candidates/add", { 
      state: { 
        cycleId: selectedCycle,
        cycleYear: selectedCycleData?.cycleYear,
        cycleName: selectedCycleData?.cycleName
      } 
    });
  };

  const handleSendInvite = () => {
    if (!selectedCycle) return;
    showToast("Invite sent to all candidates in this cycle", "success");
    // TODO: Implement actual invite logic
  };

  const handleBulkStatusUpdate = async () => {
    if (!bulkStatusUpdate || filteredCandidates.length === 0) {
      showToast("Please select a status and ensure candidates are displayed", "error");
      return;
    }

    setUpdatingBulkStatus(true);
    try {
      const candidateIds = filteredCandidates.map((c) => c.candidateId);
      const response = await candidateApi.bulkUpdateCandidateStatus({
        candidateIds,
        status: bulkStatusUpdate,
        reason: `Bulk status update to ${bulkStatusUpdate}`,
        updatedBy: 1, // TODO: Replace with actual logged-in user ID
      });

      if (response.data) {
        showToast(
          `Updated ${response.data.successCount} candidates successfully. ${response.data.failureCount} failed.`,
          response.data.failureCount > 0 ? "error" : "success"
        );
        setBulkStatusUpdate("");
        // Refresh candidates
        if (selectedCycle) {
          await fetchCandidatesByCycle(selectedCycle);
        }
      }
    } catch (error) {
      showToast("Failed to update candidate statuses", "error");
      console.error("Error updating bulk status:", error);
    } finally {
      setUpdatingBulkStatus(false);
    }
  };

  return (
    <Box className="candidates-container">
      {/* Header */}
      <Card className="candidates-header">
        <Box>
          <Typography variant="h4" className="candidates-title">
            <PersonIcon className="title-icon" />
            Candidates Management
          </Typography>
          <Typography variant="body2" className="candidates-subtitle">
            Manage and view all registered candidates
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleAddCandidate}
          className="add-candidate-btn"
        >
          Add Candidate
        </Button>
      </Card>

      {/* Cycle Selection */}
      <Card className="cycle-selector-card">
        <CardContent>
          <Box className="cycle-selector-header">
            <FormControl className="cycle-dropdown">
              <InputLabel>Select Hiring Cycle</InputLabel>
              <Select
                value={selectedCycle || ""}
                label="Select Hiring Cycle"
                onChange={(e) => setSelectedCycle(Number(e.target.value))}
              >
                {cycles.map((cycle) => (
                  <MenuItem 
                    key={cycle.cycleId} 
                    value={cycle.cycleId}
                    className={cycle.status === "OPEN" ? "cycle-status-open" : "cycle-status-closed"}
                  >
                    {cycle.cycleName} - {cycle.cycleYear}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box className="cycle-actions">
              <Button
                variant="outlined"
                startIcon={<EmailIcon />}
                onClick={handleSendInvite}
                disabled={!selectedCycle}
                className="send-invite-btn"
              >
                Send Invite
              </Button>

              <FormControl size="small" className="bulk-status-dropdown">
                <InputLabel>Bulk Update Status</InputLabel>
                <Select
                  value={bulkStatusUpdate}
                  label="Bulk Update Status"
                  onChange={(e) => setBulkStatusUpdate(e.target.value)}
                  disabled={!selectedCycle || filteredCandidates.length === 0}
                >
                  <MenuItem value="">Select Status</MenuItem>                 
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
                startIcon={<UpdateIcon />}
                onClick={handleBulkStatusUpdate}
                disabled={
                  !bulkStatusUpdate ||
                  !selectedCycle ||
                  filteredCandidates.length === 0 ||
                  updatingBulkStatus
                }
                className="bulk-update-btn"
              >
                {updatingBulkStatus ? "Updating..." : "Update"}
              </Button>
            </Box>
          </Box>
        </CardContent>
      </Card>

      {selectedCycle && (
        <>
          {/* Filters */}
          <Card className="filters-card">
            <CardContent>
              <Box className="filters-header">
                <Typography variant="h6" className="filters-title">
                  <SearchIcon className="filter-icon" />
                  Filters
                </Typography>
                {(filters.candidateName ||
                  filters.instituteName ||
                  filters.status ||
                  filters.eligibility) && (
                  <Button
                    startIcon={<ClearIcon />}
                    onClick={clearFilters}
                    size="small"
                    className="clear-filters-btn"
                  >
                    Clear All
                  </Button>
                )}
              </Box>

              <Box className="filters-grid">
                <TextField
                  label="Candidate Name"
                  variant="outlined"
                  size="small"
                  fullWidth
                  value={filters.candidateName}
                  onChange={(e) => handleFilterChange("candidateName", e.target.value)}
                />

                <FormControl size="small" fullWidth>
                  <InputLabel>Institute</InputLabel>
                  <Select
                    value={filters.instituteName}
                    label="Institute"
                    onChange={(e) => handleFilterChange("instituteName", e.target.value)}
                  >
                    <MenuItem value="">All Institutes</MenuItem>
                    {uniqueInstitutes.map((institute) => (
                      <MenuItem key={institute} value={institute}>
                        {institute}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <FormControl size="small" fullWidth>
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={filters.status}
                    label="Status"
                    onChange={(e) => handleFilterChange("status", e.target.value)}
                  >
                    <MenuItem value="">All Status</MenuItem>
                    {uniqueStatuses.map((status) => (
                      <MenuItem key={status} value={status}>
                        {status}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <FormControl size="small" fullWidth>
                  <InputLabel>Eligibility</InputLabel>
                  <Select
                    value={filters.eligibility}
                    label="Eligibility"
                    onChange={(e) => handleFilterChange("eligibility", e.target.value)}
                  >
                    <MenuItem value="">All</MenuItem>
                    <MenuItem value="eligible">Eligible</MenuItem>
                    <MenuItem value="ineligible">Ineligible</MenuItem>
                  </Select>
                </FormControl>
              </Box>

              <Typography variant="body2" className="results-count">
                Showing {filteredCandidates.length} of {allCandidates.length} candidates
              </Typography>
            </CardContent>
          </Card>

          {/* Table */}
          {candidatesLoading ? (
            <Box className="candidates-loading">
              <CircularProgress />
              <Typography>Loading candidates...</Typography>
            </Box>
          ) : filteredCandidates.length === 0 ? (
            <Card className="no-results-card">
              <CardContent className="no-results-content">
                <PersonIcon className="no-results-icon" />
                <Typography variant="h6" color="textSecondary">
                  No candidates found
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Try adjusting your filters or add a new candidate
                </Typography>
              </CardContent>
            </Card>
          ) : (
            <TableContainer component={Paper} className="candidates-table-container">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell className="table-header">Institute Name</TableCell>
                    <TableCell className="table-header">Candidate Name</TableCell>
                    <TableCell className="table-header">CGPA</TableCell>
                    <TableCell className="table-header">Passout Year</TableCell>
                    <TableCell className="table-header">No. of Arrears</TableCell>
                    <TableCell className="table-header">Status</TableCell>
                    <TableCell className="table-header" align="center">
                      Action
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredCandidates.map((candidate) => (
                    <TableRow key={candidate.candidateId} className="candidate-row">
                      <TableCell>
                        <Box className="institute-name-cell">
                          <SchoolIcon className="institute-icon-small" />
                          <Typography>{candidate.instituteName || "N/A"}</Typography>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Tooltip
                          title={candidate.reason || "No additional information"}
                          arrow
                          placement="top"
                        >
                          <Typography
                            className={
                              candidate.isEligible
                                ? "candidate-name-eligible"
                                : "candidate-name-ineligible"
                            }
                          >
                            {`${candidate.firstName} ${candidate.lastName}`}
                          </Typography>
                        </Tooltip>
                      </TableCell>
                      <TableCell>
                        <Typography>{candidate.cgpa?.toFixed(2) || "N/A"}</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography>{candidate.passoutYear || "N/A"}</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography>{candidate.historyOfArrears || 0}</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography className="status-text">{candidate.status}</Typography>
                      </TableCell>
                      <TableCell align="center">
                        <IconButton
                          size="small"
                          className="action-btn view-btn"
                          onClick={() => handleCandidateView(candidate.candidateId)}
                          title="View Details"
                        >
                          <VisibilityIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </>
      )}
    </Box>
  );
};

export default CandidateList;
