import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { instituteApi } from "../../../services/hiring.api";
import type { InstituteRequest } from "../../../types/TA_Recruiter/Hiring/institute.types";
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
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import AddIcon from "@mui/icons-material/Add";
import UploadIcon from "@mui/icons-material/Upload";
import DownloadIcon from "@mui/icons-material/Download";
import EditIcon from "@mui/icons-material/Edit";
import CloseIcon from "@mui/icons-material/Close";
import "../../../css/TA_Recruiter/Institutes/AddInstitute.css";

const AddInstitute: React.FC = () => {
  const navigate = useNavigate();
  const [addDialog, setAddDialog] = useState(false);
  const [bulkData, setBulkData] = useState<InstituteRequest[]>([]);
  const [editIndex, setEditIndex] = useState<number | null>(null);
  const [showErrorOverlay, setShowErrorOverlay] = useState(false);
  const [errorMessages, setErrorMessages] = useState<string[]>([]);
  const [singleForm, setSingleForm] = useState<InstituteRequest>({
    instituteName: "",
    instituteTier: "TIER_1",
    location: "",
    state: "",
    city: "",
    isActive: true,
  });

  const handleAddSingle = async () => {
    // Validate
    if (!singleForm.instituteName || !singleForm.city || !singleForm.state) {
      showToast("Please fill all required fields", "error");
      return;
    }
    try {
      await instituteApi.createInstitute(singleForm);
      showToast("Institute added successfully", "success");
      setAddDialog(false);
      setSingleForm({
        instituteName: "",
        instituteTier: "TIER_1",
        location: "",
        state: "",
        city: "",
        isActive: true,
      });
    } catch (error) {
      console.error(error);
      showToast("Failed to add institute", "error");
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

        const institutes: InstituteRequest[] = jsonData.map((row) => ({
          instituteName: (row["Institute Name"] || row["instituteName"] || "") as string,
          instituteTier: (row["Tier"] || row["instituteTier"] || "TIER_1") as string,
          location: (row["Location"] || row["location"] || "") as string,
          state: (row["State"] || row["state"] || "") as string,
          city: (row["City"] || row["city"] || "") as string,
          isActive: row["Active"] !== undefined ? Boolean(row["Active"]) : true,
        }));

        // Validate
        const errors: string[] = [];
        institutes.forEach((inst, idx) => {
          if (!inst.instituteName) errors.push(`Row ${idx + 1}: Missing Institute Name`);
          if (!inst.city) errors.push(`Row ${idx + 1}: Missing City`);
          if (!inst.state) errors.push(`Row ${idx + 1}: Missing State`);
        });

        if (errors.length > 0) {
          showToast(`Validation errors: ${errors.join(", ")}`, "error");
        } else {
          setBulkData(institutes);
          showToast(`${institutes.length} institutes loaded`, "success");
        }
      } catch (error) {
        console.error(error);
        showToast("Failed to read file", "error");
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
      const response = await instituteApi.bulkCreateInstitutes(bulkData);
      console.log("Bulk upload response:", response);
      
      // Check if there are any errors in the response
      if (response.data.errorMessages && response.data.errorMessages.length > 0) {
        // Show error overlay
        setErrorMessages(response.data.errorMessages);
        setShowErrorOverlay(true);
        
        // Show success toast if some were successful
        if (response.data.successfulInserts && response.data.successfulInserts.length > 0) {
          showToast(`${response.data.successfulInserts.length} institutes uploaded, ${response.data.errorMessages.length} failed`, "error");
        }
      } else {
        showToast(`${bulkData.length} institutes uploaded successfully`, "success");
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
      } else {
        showToast(err.message || "Upload failed", "error");
      }
    }
  };

  const handleDownloadFormat = () => {
    const link = document.createElement("a");
    link.href = "/files/college_Data.xlsx";
    link.download = "college_Data.xlsx";
    link.click();
  };

  const handleEditCell = (index: number, field: keyof InstituteRequest, value: string | boolean) => {
    const updated = [...bulkData];
    updated[index] = { ...updated[index], [field]: value };
    setBulkData(updated);
  };

  return (
    <Box className="add-institute-container">
      {/* Header */}
      <Box className="add-institute-header">
        <IconButton onClick={() => navigate("/ta-recruiter/institutes")} className="back-btn">
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4">Add Institutes</Typography>
      </Box>

      {/* Action Buttons */}
      <Card className="add-institute-action-card">
        <CardContent>
          <Box className="add-institute-actions-container">
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setAddDialog(true)}
              className="add-institute-action-btn add-institute-add-btn"
            >
              Add Institute
            </Button>

            <Button
              variant="contained"
              component="label"
              startIcon={<UploadIcon />}
              className="add-institute-action-btn add-institute-upload-btn"
            >
              Upload Institutes
              <input type="file" hidden accept=".xlsx,.xls" onChange={handleFileUpload} />
            </Button>

            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              onClick={handleDownloadFormat}
              className="add-institute-action-btn add-institute-download-btn"
            >
              Download Format
            </Button>
          </Box>
        </CardContent>
      </Card>

      {/* Bulk Data Table */}
      {bulkData.length > 0 && (
        <Card className="add-institute-bulk-card">
          <CardContent>
            <Box className="add-institute-bulk-header">
              <Typography variant="h6">Uploaded Data ({bulkData.length} institutes)</Typography>
              <Button variant="contained" onClick={handleBulkUpload} className="add-institute-bulk-upload-btn">
                Upload to Database
              </Button>
            </Box>

            <TableContainer component={Paper} className="add-institute-bulk-table">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell className="table-header">Institute Name</TableCell>
                    <TableCell className="table-header">Tier</TableCell>
                    <TableCell className="table-header">City</TableCell>
                    <TableCell className="table-header">State</TableCell>
                    <TableCell className="table-header">Location URL</TableCell>
                    <TableCell className="table-header">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {bulkData.map((inst, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        {editIndex === index ? (
                          <TextField
                            size="small"
                            value={inst.instituteName}
                            onChange={(e) => handleEditCell(index, "instituteName", e.target.value)}
                          />
                        ) : (
                          inst.instituteName
                        )}
                      </TableCell>
                      <TableCell>
                        {editIndex === index ? (
                          <Select
                            size="small"
                            value={inst.instituteTier}
                            onChange={(e) => handleEditCell(index, "instituteTier", e.target.value)}
                          >
                            <MenuItem value="TIER_1">TIER 1</MenuItem>
                            <MenuItem value="TIER_2">TIER 2</MenuItem>
                            <MenuItem value="TIER_3">TIER 3</MenuItem>
                          </Select>
                        ) : (
                          inst.instituteTier
                        )}
                      </TableCell>
                      <TableCell>
                        {editIndex === index ? (
                          <TextField
                            size="small"
                            value={inst.city}
                            onChange={(e) => handleEditCell(index, "city", e.target.value)}
                          />
                        ) : (
                          inst.city
                        )}
                      </TableCell>
                      <TableCell>
                        {editIndex === index ? (
                          <TextField
                            size="small"
                            value={inst.state}
                            onChange={(e) => handleEditCell(index, "state", e.target.value)}
                          />
                        ) : (
                          inst.state
                        )}
                      </TableCell>
                      <TableCell>
                        {editIndex === index ? (
                          <TextField
                            size="small"
                            value={inst.location}
                            onChange={(e) => handleEditCell(index, "location", e.target.value)}
                          />
                        ) : (
                          inst.location
                        )}
                      </TableCell>
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

      {/* Add Single Institute Dialog */}
      <Dialog open={addDialog} onClose={() => setAddDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Institute</DialogTitle>
        <DialogContent>
          <Box className="add-institute-form">
            <TextField
              label="Institute Name *"
              fullWidth
              value={singleForm.instituteName}
              onChange={(e) => setSingleForm({ ...singleForm, instituteName: e.target.value })}
            />
            <FormControl fullWidth>
              <InputLabel>Tier *</InputLabel>
              <Select
                value={singleForm.instituteTier}
                label="Tier"
                onChange={(e) => setSingleForm({ ...singleForm, instituteTier: e.target.value })}
              >
                <MenuItem value="TIER_1">TIER 1</MenuItem>
                <MenuItem value="TIER_2">TIER 2</MenuItem>
                <MenuItem value="TIER_3">TIER 3</MenuItem>
              </Select>
            </FormControl>
          
            <TextField
              label="State *"
              fullWidth
              value={singleForm.state}
              onChange={(e) => setSingleForm({ ...singleForm, state: e.target.value })}
            />
              <TextField
              label="City *"
              fullWidth
              value={singleForm.city}
              onChange={(e) => setSingleForm({ ...singleForm, city: e.target.value })}
            />
            <TextField
              label="Location (Google Maps URL)"
              fullWidth
              value={singleForm.location}
              onChange={(e) => setSingleForm({ ...singleForm, location: e.target.value })}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddDialog(false)}>Cancel</Button>
          <Button onClick={handleAddSingle} variant="contained">
            Add Institute
          </Button>
        </DialogActions>
      </Dialog>

      {/* Error Overlay */}
      {showErrorOverlay && (
        <Box
          sx={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999,
          }}
          onClick={() => setShowErrorOverlay(false)}
        >
          <Box
            sx={{
              backgroundColor: 'var(--color-surface)',
              borderRadius: '12px',
              padding: '24px',
              maxWidth: '600px',
              width: '90%',
              maxHeight: '80vh',
              overflow: 'auto',
              position: 'relative',
              border: '1px solid var(--color-border)',
              boxShadow: 'var(--shadow-card)',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <Typography variant="h6" sx={{ color: 'var(--color-danger)', fontWeight: 600 }}>
                Validation Errors ({errorMessages.length})
              </Typography>
              <IconButton onClick={() => setShowErrorOverlay(false)} size="small">
                <CloseIcon />
              </IconButton>
            </Box>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {errorMessages.map((error, index) => (
                <Box
                  key={index}
                  sx={{
                    padding: '12px',
                    backgroundColor: 'var(--color-bg)',
                    borderRadius: '8px',
                    border: '1px solid var(--color-border)',
                    display: 'flex',
                    gap: '8px',
                  }}
                >
                  <Typography
                    sx={{
                      fontWeight: 600,
                      color: 'var(--color-danger)',
                      minWidth: '24px',
                    }}
                  >
                    {index + 1}.
                  </Typography>
                  <Typography sx={{ color: 'var(--color-text)', fontSize: '14px' }}>
                    {error}
                  </Typography>
                </Box>
              ))}
            </Box>
          </Box>
        </Box>
      )}
    </Box>
  );
};

export default AddInstitute;
