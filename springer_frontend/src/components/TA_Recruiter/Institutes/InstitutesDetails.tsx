import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { instituteApi, instituteTPOApi } from "../../../services/hiring.api";
import type { InstituteWithTPOsResponse, TPODetails } from "../../../types/TA_Recruiter/Hiring/institute.types";
import { showToast } from "../../../utils/toast";
import {
  Box,
  Card,
  CardContent,
  CircularProgress,
  Typography,
  Chip,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  FormControlLabel,
  Switch,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import SchoolIcon from "@mui/icons-material/School";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import EmailIcon from "@mui/icons-material/Email";
import PhoneIcon from "@mui/icons-material/Phone";
import EditIcon from "@mui/icons-material/Edit";
import "../../../css/TA_Recruiter/Institutes/InstitutesDetails.css";

const InstitutesDetails: React.FC = () => {
  const { instituteId } = useParams<{ instituteId: string }>();
  const navigate = useNavigate();
  const [data, setData] = useState<InstituteWithTPOsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [editDialog, setEditDialog] = useState(false);
  const [editTPO, setEditTPO] = useState<TPODetails | null>(null);
  const [editForm, setEditForm] = useState({
    tpoName: "",
    tpoEmail: "",
    tpoMobile: "",
    isPrimary: false,
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await instituteApi.getInstituteWithTPOsById(Number(instituteId));
        setData(response.data);
      } catch (error) {
        console.log(error);
        showToast("Failed to load institute details: " , "error");
      } finally {
        setLoading(false);
      }
    };
    
    if (instituteId) fetchData();
  }, [instituteId]);

  const handleEditClick = (tpo: TPODetails) => {
    setEditTPO(tpo);
    setEditForm({
      tpoName: tpo.tpoName,
      tpoEmail: tpo.tpoEmail,
      tpoMobile: tpo.tpoMobile,
      isPrimary: tpo.isPrimary,
    });
    setEditDialog(true);
  };

  const handleEditSave = async () => {
    if (!editTPO) return;
    try {
      await instituteTPOApi.updateContact(editTPO.tpoId, editForm);
      showToast("TPO contact updated successfully", "success");
      setEditDialog(false);
      // Refresh data
      const response = await instituteApi.getInstituteWithTPOsById(Number(instituteId));
      setData(response.data);
    } catch (error) {
      console.error(error);
      showToast("Failed to update TPO contact", "error");
    }
  };

  const handleToggleStatus = async (tpoId: number, currentStatus: string) => {
    try {
      await instituteTPOApi.deleteContact(tpoId);
      showToast(`TPO contact ${currentStatus === 'ACTIVE' ? 'deactivated' : 'activated'} successfully`, "success");
      // Refresh data
      const response = await instituteApi.getInstituteWithTPOsById(Number(instituteId));
      setData(response.data);
    } catch (error) {
      console.error(error);
      showToast("Failed to toggle TPO status", "error");
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "400px" }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!data) {
    return (
      <Box sx={{ p: 3, textAlign: "center" }}>
        <Typography variant="h6">Institute not found</Typography>
      </Box>
    );
  }

  return (
    <Box className="institute-details-container">
      {/* Back Button */}
      <IconButton onClick={() => navigate("/ta-recruiter/institutes")} sx={{ mb: 2 }}>
        <ArrowBackIcon />
      </IconButton>

      {/* Institute Info */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: "flex", gap: 2, alignItems: "start", mb: 2 }}>
            <SchoolIcon sx={{ fontSize: 40, color: "var(--color-primary)" }} />
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 600, mb: 1 }}>
                {data.instituteName}
              </Typography>
              <Box sx={{ display: "flex", gap: 1, mb: 2 }}>
                <Chip label={data.instituteTier} size="small" color="primary" />
                <Chip 
                  label={data.isActive ? "Active" : "Inactive"} 
                  size="small" 
                  color={data.isActive ? "success" : "default"} 
                />
              </Box>
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <LocationOnIcon sx={{ fontSize: 20, color: "var(--color-accent)" }} />
                <Typography variant="body1">
                  {data.city}, {data.state}
                </Typography>
              </Box>
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* TPO List */}
      <Card>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
            TPO Contacts ({data.tpoDetails.length})
          </Typography>
          
          {data.tpoDetails.length === 0 ? (
            <Typography color="textSecondary">No TPO contacts available</Typography>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 600 }}>Name</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Email</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Mobile</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Primary</TableCell>
                    <TableCell sx={{ fontWeight: 600 }} align="center">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {data.tpoDetails.map((tpo) => (
                    <TableRow key={tpo.tpoId}>
                      <TableCell>{tpo.tpoName}</TableCell>
                      <TableCell>
                        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                          <EmailIcon sx={{ fontSize: 18, color: "var(--color-accent)" }} />
                          {tpo.tpoEmail}
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                          <PhoneIcon sx={{ fontSize: 18, color: "var(--color-accent)" }} />
                          {tpo.tpoMobile}
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={tpo.tpoStatus} 
                          size="small" 
                          color={tpo.tpoStatus === "ACTIVE" ? "success" : "default"}
                          onClick={() => handleToggleStatus(tpo.tpoId, tpo.tpoStatus)}
                          sx={{ cursor: "pointer" }}
                        />
                      </TableCell>
                      <TableCell>
                        {tpo.isPrimary && (
                          <Chip label="Primary" size="small" color="warning" />
                        )}
                      </TableCell>
                      <TableCell align="center">
                        <IconButton
                          size="small"
                          onClick={() => handleEditClick(tpo)}
                          sx={{ color: "var(--color-primary)" }}
                        >
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* Edit TPO Dialog */}
      <Dialog open={editDialog} onClose={() => setEditDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Edit TPO Contact</DialogTitle>
        <DialogContent>
          <Box sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 2 }}>
            <TextField
              label="TPO Name"
              fullWidth
              value={editForm.tpoName}
              onChange={(e) => setEditForm({ ...editForm, tpoName: e.target.value })}
            />
            <TextField
              label="Email"
              fullWidth
              type="email"
              value={editForm.tpoEmail}
              onChange={(e) => setEditForm({ ...editForm, tpoEmail: e.target.value })}
            />
            <TextField
              label="Mobile"
              fullWidth
              value={editForm.tpoMobile}
              onChange={(e) => setEditForm({ ...editForm, tpoMobile: e.target.value })}
            />
            <FormControlLabel
              control={
                <Switch
                  checked={editForm.isPrimary}
                  onChange={(e) => setEditForm({ ...editForm, isPrimary: e.target.checked })}
                />
              }
              label="Primary Contact"
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

export default InstitutesDetails;
