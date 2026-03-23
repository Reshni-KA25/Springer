import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { instituteApi } from "../../../services/hiring.api";
import type { InstituteResponse } from "../../../types/TA_Recruiter/Hiring/institute.types";
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
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Switch,
  FormControlLabel,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import SearchIcon from "@mui/icons-material/Search";
import ClearIcon from "@mui/icons-material/Clear";
import SchoolIcon from "@mui/icons-material/School";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import VisibilityIcon from "@mui/icons-material/Visibility";
import EmailIcon from "@mui/icons-material/Email";
import EditIcon from "@mui/icons-material/Edit";
import "../../../css/TA_Recruiter/Institutes/InstitutesList.css";

interface Filters {
  instituteName: string;
  state: string;
  city: string;
  instituteTier: string;
  status: string;
}

const InstitutesList: React.FC = () => {
  const navigate = useNavigate();
  const [allInstitutes, setAllInstitutes] = useState<InstituteResponse[]>([]);
  const [filteredInstitutes, setFilteredInstitutes] = useState<InstituteResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [filters, setFilters] = useState<Filters>({
    instituteName: "",
    state: "",
    city: "",
    instituteTier: "",
    status: "",
  });
  const [editDialog, setEditDialog] = useState(false);
  const [editInstitute, setEditInstitute] = useState<InstituteResponse | null>(null);
  const [editForm, setEditForm] = useState({
    instituteName: "",
    city: "",
    state: "",
    location: "",
    instituteTier: "",
    isActive: true,
  });

  // Extract unique values for filter dropdowns
  const uniqueStates = Array.from(new Set(allInstitutes.map((inst) => inst.state))).filter(Boolean);
  const uniqueCities = Array.from(new Set(allInstitutes.map((inst) => inst.city))).filter(Boolean);
  const uniqueTiers = Array.from(new Set(allInstitutes.map((inst) => inst.instituteTier))).filter(Boolean);

  useEffect(() => {
    fetchInstitutes();
  }, []);

  const fetchInstitutes = async () => {
    try {
      const response = await instituteApi.getAllInstitutes();
      if (response.data) {
        setAllInstitutes(response.data);
        setFilteredInstitutes(response.data);
      }
    } catch (error) {
      showToast("Failed to fetch institutes", "error");
      console.error("Error fetching institutes:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let filtered = [...allInstitutes];

    if (filters.instituteName) {
      filtered = filtered.filter((institute) =>
        institute.instituteName.toLowerCase().includes(filters.instituteName.toLowerCase())
      );
    }

    if (filters.state) {
      filtered = filtered.filter((institute) => institute.state === filters.state);
    }

    if (filters.city) {
      filtered = filtered.filter((institute) => institute.city === filters.city);
    }

    if (filters.instituteTier) {
      filtered = filtered.filter((institute) => institute.instituteTier === filters.instituteTier);
    }

    if (filters.status) {
      const isActive = filters.status === "active";
      filtered = filtered.filter((institute) => institute.isActive === isActive);
    }

    setFilteredInstitutes(filtered);
  }, [filters, allInstitutes]);

  const handleFilterChange = (field: keyof Filters, value: string) => {
    setFilters((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const clearFilters = () => {
    setFilters({
      instituteName: "",
      state: "",
      city: "",
      instituteTier: "",
      status: "",
    });
  };

  const handleInstituteClick = (instituteId: number) => {
    navigate(`/ta-recruiter/institutes/${instituteId}`);
  };

  const handleInvite = (instituteId: number, instituteName: string) => {
    // TODO: Implement invite functionality
    showToast(`Invite sent to ${instituteName}`, "success");
    console.log("Invite institute:", instituteId);
  };

  const handleAddInstitute = () => {
    navigate("/ta-recruiter/institutes/add");
  };

  const handleEditClick = (institute: InstituteResponse) => {
    setEditInstitute(institute);
    setEditForm({
      instituteName: institute.instituteName,
      city: institute.city,
      state: institute.state,
      location: institute.location,
      instituteTier: institute.instituteTier,
      isActive: institute.isActive,
    });
    setEditDialog(true);
  };

  const handleEditSave = async () => {
    if (!editInstitute) return;
    try {
      await instituteApi.updateInstitute(editInstitute.instituteId, editForm);
      showToast("Institute updated successfully", "success");
      setEditDialog(false);
      fetchInstitutes();
    } catch (error) {
      console.error(error);
      showToast("Failed to update institute", "error");
    }
  };

  const handleToggleStatus = async (institute: InstituteResponse) => {
    try {
      await instituteApi.deleteInstitute(institute.instituteId);
      showToast(`Institute ${institute.isActive ? 'deactivated' : 'activated'} successfully`, "success");
      fetchInstitutes();
    } catch (error) {
      console.error(error);
      showToast("Failed to toggle institute status", "error");
    }
  };

  const getTierColor = (tier: string): "default" | "primary" | "secondary" | "success" => {
    switch (tier) {
      case "TIER_1":
        return "success";
      case "TIER_2":
        return "primary";
      case "TIER_3":
        return "secondary";
      default:
        return "default";
    }
  };

  const getStatusColor = (isActive: boolean): "success" | "default" => {
    return isActive ? "success" : "default";
  };

  if (loading) {
    return (
      <Box className="institutes-loading">
        <CircularProgress />
        <Typography>Loading institutes...</Typography>
      </Box>
    );
  }

  return (
    <Box className="institutes-container">
      {/* Header */}
      <Card className="institutes-header">
        <Box>
          <Typography variant="h4" className="institutes-title">
            <SchoolIcon className="title-icon" />
            Institutes Management
          </Typography>
          <Typography variant="body2" className="institutes-subtitle">
            Manage and view all registered institutes
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleAddInstitute}
          className="add-institute-btn"
        >
          Add Institute
        </Button>
      </Card>

      {/* Filters */}
      <Card className="filters-card">
        <CardContent>
          <Box className="filters-header">
            <Typography variant="h6" className="filters-title">
              <SearchIcon className="filter-icon" />
              Filters
            </Typography>
            {(filters.instituteName ||
              filters.state ||
              filters.city ||
              filters.instituteTier ||
              filters.status) && (
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
              label="Institute Name"
              variant="outlined"
              size="small"
              fullWidth
              value={filters.instituteName}
              onChange={(e) => handleFilterChange("instituteName", e.target.value)}
            />

            <FormControl size="small" fullWidth>
              <InputLabel>State</InputLabel>
              <Select
                value={filters.state}
                label="State"
                onChange={(e) => handleFilterChange("state", e.target.value)}
              >
                <MenuItem value="">All States</MenuItem>
                {uniqueStates.map((state) => (
                  <MenuItem key={state} value={state}>
                    {state}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl size="small" fullWidth>
              <InputLabel>City</InputLabel>
              <Select
                value={filters.city}
                label="City"
                onChange={(e) => handleFilterChange("city", e.target.value)}
              >
                <MenuItem value="">All Cities</MenuItem>
                {uniqueCities.map((city) => (
                  <MenuItem key={city} value={city}>
                    {city}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl size="small" fullWidth>
              <InputLabel>Tier</InputLabel>
              <Select
                value={filters.instituteTier}
                label="Tier"
                onChange={(e) => handleFilterChange("instituteTier", e.target.value)}
              >
                <MenuItem value="">All Tiers</MenuItem>
                {uniqueTiers.map((tier) => (
                  <MenuItem key={tier} value={tier}>
                    {tier}
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
                <MenuItem value="active">Active</MenuItem>
                <MenuItem value="inactive">Inactive</MenuItem>
              </Select>
            </FormControl>
          </Box>

          <Typography variant="body2" className="results-count">
            Showing {filteredInstitutes.length} of {allInstitutes.length} institutes
          </Typography>
        </CardContent>
      </Card>

      {/* Table */}
      {filteredInstitutes.length === 0 ? (
        <Card className="no-results-card">
          <CardContent className="no-results-content">
            <SchoolIcon className="no-results-icon" />
            <Typography variant="h6" color="textSecondary">
              No institutes found
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Try adjusting your filters or add a new institute
            </Typography>
          </CardContent>
        </Card>
      ) : (
        <TableContainer component={Paper} className="institutes-table-container">
          <Table>
            <TableHead>
              <TableRow>
                <TableCell className="table-header">Institute</TableCell>
                <TableCell className="table-header">Tier</TableCell>
                <TableCell className="table-header">Location</TableCell>
                <TableCell className="table-header">Status</TableCell>
                <TableCell className="table-header" align="center">
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredInstitutes.map((institute) => (
                <TableRow key={institute.instituteId} className="institute-row">
                  <TableCell>
                    <Box className="institute-name-cell">
                      <SchoolIcon className="institute-table-icon" />
                      <Box>
                        <Typography className="institute-table-name">
                          {institute.instituteName}
                        </Typography>
                        <Typography variant="caption" className="institute-table-date">
                          Added: {new Date(institute.createdAt).toLocaleDateString()}
                        </Typography>
                      </Box>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={institute.instituteTier}
                      color={getTierColor(institute.instituteTier)}
                      size="small"
                      className="tier-chip-table"
                    />
                  </TableCell>
                  <TableCell>
                    <Box className="location-cell">
                      <LocationOnIcon className="location-table-icon" />
                      <Typography variant="body2">
                        {institute.city}, {institute.state}
                      </Typography>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={institute.isActive ? "Active" : "Inactive"}
                      color={getStatusColor(institute.isActive)}
                      size="small"
                      className="status-chip-table"
                      onClick={() => handleToggleStatus(institute)}
                      sx={{ cursor: "pointer" }}
                    />
                  </TableCell>
                  <TableCell align="center">
                    <Box className="action-buttons">
                      <IconButton
                        size="small"
                        className="action-btn view-btn"
                        onClick={() => handleInstituteClick(institute.instituteId)}
                        title="View Details"
                      >
                        <VisibilityIcon fontSize="small" />
                      </IconButton>
                      <IconButton
                        size="small"
                        className="action-btn"
                        onClick={() => handleEditClick(institute)}
                        title="Edit Institute"
                        sx={{ color: "var(--color-warning)" }}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                      <IconButton
                        size="small"
                        className="action-btn invite-btn"
                        onClick={() => handleInvite(institute.instituteId, institute.instituteName)}
                        title="Send Invite"
                      >
                        <EmailIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Edit Dialog */}
      <Dialog open={editDialog} onClose={() => setEditDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Institute</DialogTitle>
        <DialogContent>
          <Box sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 2 }}>
            <TextField
              label="Institute Name"
              fullWidth
              value={editForm.instituteName}
              onChange={(e) => setEditForm({ ...editForm, instituteName: e.target.value })}
            />
            <FormControl fullWidth>
              <InputLabel>Tier</InputLabel>
              <Select
                value={editForm.instituteTier}
                label="Tier"
                onChange={(e) => setEditForm({ ...editForm, instituteTier: e.target.value })}
              >
                <MenuItem value="TIER_1">TIER 1</MenuItem>
                <MenuItem value="TIER_2">TIER 2</MenuItem>
                <MenuItem value="TIER_3">TIER 3</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="City"
              fullWidth
              value={editForm.city}
              onChange={(e) => setEditForm({ ...editForm, city: e.target.value })}
            />
            <TextField
              label="State"
              fullWidth
              value={editForm.state}
              onChange={(e) => setEditForm({ ...editForm, state: e.target.value })}
            />
            <TextField
              label="Location (Google Maps URL)"
              fullWidth
              value={editForm.location}
              onChange={(e) => setEditForm({ ...editForm, location: e.target.value })}
            />
            <FormControlLabel
              control={
                <Switch
                  checked={editForm.isActive}
                  onChange={(e) => setEditForm({ ...editForm, isActive: e.target.checked })}
                />
              }
              label="Active Status"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialog(false)}>Cancel</Button>
          <Button onClick={handleEditSave} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default InstitutesList;
