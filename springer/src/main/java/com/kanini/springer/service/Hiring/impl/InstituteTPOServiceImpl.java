package com.kanini.springer.service.Hiring.impl;

import com.kanini.springer.dto.Hiring.BulkInsertResponse;
import com.kanini.springer.dto.Hiring.BulkInsertResponse.BulkInsertError;
import com.kanini.springer.dto.Hiring.InstituteContactRequest;
import com.kanini.springer.dto.Hiring.InstituteContactResponse;
import com.kanini.springer.entity.HiringReq.Institute;
import com.kanini.springer.entity.HiringReq.InstituteContact;
import com.kanini.springer.entity.enums.Enums.ContactStatus;
import com.kanini.springer.mapper.Hiring.InstituteContactMapper;
import com.kanini.springer.repository.Hiring.InstituteContactRepository;
import com.kanini.springer.repository.Hiring.InstituteRepository;
import com.kanini.springer.service.Hiring.IInstituteTPOService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstituteTPOServiceImpl implements IInstituteTPOService {
    
    private final InstituteContactRepository contactRepository;
    private final InstituteRepository instituteRepository;
    private final InstituteContactMapper mapper;
    
    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
        "^[6-9][0-9]{9}$"
    );
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[A-Za-z ]+$"
    );
    
    @Override
    @Transactional
    public InstituteContactResponse createContact(InstituteContactRequest request) {
        // Validate that the institute exists
        Institute institute = instituteRepository.findById(request.getInstituteId())
                .orElseThrow(() -> new RuntimeException("Institute not found with ID: " + request.getInstituteId()));
        
        // Validate that email is unique
        if (contactRepository.findByTpoEmail(request.getTpoEmail()).isPresent()) {
            throw new RuntimeException("Contact already exists with email: " + request.getTpoEmail());
        }
        
        InstituteContact contact = new InstituteContact();
        contact.setInstitute(institute);
        contact.setTpoName(request.getTpoName());
        contact.setTpoEmail(request.getTpoEmail());
        contact.setTpoMobile(request.getTpoMobile());
        
        if (request.getTpoStatus() != null && !request.getTpoStatus().isBlank()) {
            contact.setTpoStatus(ContactStatus.valueOf(request.getTpoStatus()));
        } else {
            contact.setTpoStatus(ContactStatus.ACTIVE);
        }
        
        contact.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        
        InstituteContact savedContact = contactRepository.save(contact);
        return mapper.toResponse(savedContact);
    }
    
    @Override
    @Transactional
    public BulkInsertResponse<InstituteContactResponse> bulkCreateContacts(Long instituteId, List<InstituteContactRequest> requests) {
        List<InstituteContact> contactsToInsert = new ArrayList<>();
        List<BulkInsertError> errors = new ArrayList<>();
        
        // Validate that the institute exists
        Institute institute = instituteRepository.findById(instituteId).orElse(null);
        if (institute == null) {
            errors.add(new BulkInsertError("Institute ID: " + instituteId, "Institute not found"));
            return new BulkInsertResponse<>(new ArrayList<>(), errors);
        }
        
        // Phase 1: Validate ALL records first
        for (int i = 0; i < requests.size(); i++) {
            InstituteContactRequest request = requests.get(i);
            String identifier = request.getTpoEmail() != null ? request.getTpoEmail() : 
                               request.getTpoName() != null ? request.getTpoName() : "Record #" + (i + 1);
            
            try {
                // Validate required fields
                if (request.getTpoName() == null || request.getTpoName().isBlank()) {
                    errors.add(new BulkInsertError(identifier, "TPO name is required"));
                    continue;
                }
                
                if (request.getTpoEmail() == null || request.getTpoEmail().isBlank()) {
                    errors.add(new BulkInsertError(identifier, "TPO email is required"));
                    continue;
                }
                
                if (request.getTpoMobile() == null || request.getTpoMobile().isBlank()) {
                    errors.add(new BulkInsertError(identifier, "TPO mobile is required"));
                    continue;
                }
                
                // Validate patterns
                if (!NAME_PATTERN.matcher(request.getTpoName()).matches()) {
                    errors.add(new BulkInsertError(identifier, "TPO name must contain only letters and spaces"));
                    continue;
                }
                
                if (!EMAIL_PATTERN.matcher(request.getTpoEmail()).matches()) {
                    errors.add(new BulkInsertError(identifier, "Invalid email format"));
                    continue;
                }
                
                if (!MOBILE_PATTERN.matcher(request.getTpoMobile()).matches()) {
                    errors.add(new BulkInsertError(identifier, "Invalid mobile number (must be 10-digit Indian number starting with 6-9)"));
                    continue;
                }
                
                // Check if email already exists
                if (contactRepository.findByTpoEmail(request.getTpoEmail()).isPresent()) {
                    errors.add(new BulkInsertError(identifier, "Contact already exists with this email"));
                    continue;
                }
                
                // Validate enum if provided
                if (request.getTpoStatus() != null && !request.getTpoStatus().isBlank()) {
                    try {
                        ContactStatus.valueOf(request.getTpoStatus());
                    } catch (IllegalArgumentException e) {
                        errors.add(new BulkInsertError(identifier, "Invalid TPO status: " + request.getTpoStatus()));
                        continue;
                    }
                }
                
                // Prepare contact for insertion
                InstituteContact contact = new InstituteContact();
                contact.setInstitute(institute);
                contact.setTpoName(request.getTpoName());
                contact.setTpoEmail(request.getTpoEmail());
                contact.setTpoMobile(request.getTpoMobile());
                
                if (request.getTpoStatus() != null && !request.getTpoStatus().isBlank()) {
                    contact.setTpoStatus(ContactStatus.valueOf(request.getTpoStatus()));
                } else {
                    contact.setTpoStatus(ContactStatus.ACTIVE);
                }
                
                contact.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
                
                contactsToInsert.add(contact);
                
            } catch (Exception e) {
                errors.add(new BulkInsertError(identifier, "Validation error: " + e.getMessage()));
            }
        }
        
        // Phase 2: If ANY errors exist, rollback and return errors (all-or-nothing)
        if (!errors.isEmpty()) {
            return new BulkInsertResponse<>(new ArrayList<>(), errors);
        }
        
        // Phase 3: Insert all records (within transaction, will auto-rollback on exception)
        List<InstituteContact> savedContacts = contactRepository.saveAll(contactsToInsert);
        List<InstituteContactResponse> responses = savedContacts.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        
        return new BulkInsertResponse<>(responses, new ArrayList<>());
    }
    
    @Override
    public List<InstituteContactResponse> getContactsByInstituteId(Long instituteId) {
        // Validate that institute exists
        if (!instituteRepository.existsById(instituteId)) {
            throw new RuntimeException("Institute not found with ID: " + instituteId);
        }
        
        return contactRepository.findByInstituteInstituteId(instituteId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public InstituteContactResponse getContactById(Integer tpoId) {
        InstituteContact contact = contactRepository.findByIdWithInstitute(tpoId)
                .orElseThrow(() -> new RuntimeException("Contact not found with ID: " + tpoId));
        return mapper.toResponse(contact);
    }
    
    @Override
    @Transactional
    public InstituteContactResponse updateContact(Integer tpoId, InstituteContactRequest request) {
        InstituteContact contact = contactRepository.findById(tpoId)
                .orElseThrow(() -> new RuntimeException("Contact not found with ID: " + tpoId));
        
        // Partial update - only update fields that are provided
        if (request.getInstituteId() != null) {
            Institute institute = instituteRepository.findById(request.getInstituteId())
                    .orElseThrow(() -> new RuntimeException("Institute not found with ID: " + request.getInstituteId()));
            contact.setInstitute(institute);
        }
        
        if (request.getTpoName() != null && !request.getTpoName().isBlank()) {
            contact.setTpoName(request.getTpoName());
        }
        
        if (request.getTpoEmail() != null && !request.getTpoEmail().isBlank()) {
            // Check if new email already exists (excluding current contact)
            contactRepository.findByTpoEmail(request.getTpoEmail())
                    .ifPresent(existingContact -> {
                        if (!existingContact.getTpoId().equals(tpoId)) {
                            throw new RuntimeException("Contact already exists with email: " + request.getTpoEmail());
                        }
                    });
            contact.setTpoEmail(request.getTpoEmail());
        }
        
        if (request.getTpoMobile() != null && !request.getTpoMobile().isBlank()) {
            contact.setTpoMobile(request.getTpoMobile());
        }
        
        if (request.getTpoStatus() != null && !request.getTpoStatus().isBlank()) {
            contact.setTpoStatus(ContactStatus.valueOf(request.getTpoStatus()));
        }
        
        if (request.getIsPrimary() != null) {
            contact.setIsPrimary(request.getIsPrimary());
        }
        
        InstituteContact updatedContact = contactRepository.save(contact);
        return mapper.toResponse(updatedContact);
    }
    
    @Override
    @Transactional
    public void deleteContact(Integer tpoId) {
        InstituteContact contact = contactRepository.findById(tpoId)
                .orElseThrow(() -> new RuntimeException("Contact not found with ID: " + tpoId));
        
        // Toggle tpoStatus between ACTIVE and INACTIVE
        if (contact.getTpoStatus() == ContactStatus.ACTIVE) {
            contact.setTpoStatus(ContactStatus.INACTIVE);
        } else {
            contact.setTpoStatus(ContactStatus.ACTIVE);
        }
        contactRepository.save(contact);
    }
    
    @Override
    @Transactional
    public BulkInsertResponse<InstituteContactResponse> bulkCreateAllContacts(List<InstituteContactRequest> requests) {
        List<InstituteContact> contactsToInsert = new ArrayList<>();
        List<BulkInsertError> errors = new ArrayList<>();
        
        // Phase 1: Validate ALL records first
        for (int i = 0; i < requests.size(); i++) {
            InstituteContactRequest request = requests.get(i);
            String identifier = request.getTpoEmail() != null ? request.getTpoEmail() : 
                               request.getTpoName() != null ? request.getTpoName() : "Record #" + (i + 1);
            
            try {
                // Validate required fields
                if (request.getInstituteId() == null) {
                    errors.add(new BulkInsertError(identifier, "Institute ID is required"));
                    continue;
                }
                
                if (request.getTpoName() == null || request.getTpoName().isBlank()) {
                    errors.add(new BulkInsertError(identifier, "TPO name is required"));
                    continue;
                }
                
                if (request.getTpoEmail() == null || request.getTpoEmail().isBlank()) {
                    errors.add(new BulkInsertError(identifier, "TPO email is required"));
                    continue;
                }
                
                if (request.getTpoMobile() == null || request.getTpoMobile().isBlank()) {
                    errors.add(new BulkInsertError(identifier, "TPO mobile is required"));
                    continue;
                }
                
                // Validate that the institute exists
                Institute institute = instituteRepository.findById(request.getInstituteId()).orElse(null);
                if (institute == null) {
                    errors.add(new BulkInsertError(identifier, "Institute not found with ID: " + request.getInstituteId()));
                    continue;
                }
                
                // Validate patterns
                if (!NAME_PATTERN.matcher(request.getTpoName()).matches()) {
                    errors.add(new BulkInsertError(identifier, "TPO name must contain only letters and spaces"));
                    continue;
                }
                
                if (!EMAIL_PATTERN.matcher(request.getTpoEmail()).matches()) {
                    errors.add(new BulkInsertError(identifier, "Invalid email format"));
                    continue;
                }
                
                if (!MOBILE_PATTERN.matcher(request.getTpoMobile()).matches()) {
                    errors.add(new BulkInsertError(identifier, "Invalid mobile number (must be 10-digit Indian number starting with 6-9)"));
                    continue;
                }
                
                // Check if email already exists
                if (contactRepository.findByTpoEmail(request.getTpoEmail()).isPresent()) {
                    errors.add(new BulkInsertError(identifier, "Contact already exists with this email"));
                    continue;
                }
                
                // Validate enum if provided
                if (request.getTpoStatus() != null && !request.getTpoStatus().isBlank()) {
                    try {
                        ContactStatus.valueOf(request.getTpoStatus());
                    } catch (IllegalArgumentException e) {
                        errors.add(new BulkInsertError(identifier, "Invalid TPO status: " + request.getTpoStatus()));
                        continue;
                    }
                }
                
                // Prepare contact for insertion
                InstituteContact contact = new InstituteContact();
                contact.setInstitute(institute);
                contact.setTpoName(request.getTpoName());
                contact.setTpoEmail(request.getTpoEmail());
                contact.setTpoMobile(request.getTpoMobile());
                
                if (request.getTpoStatus() != null && !request.getTpoStatus().isBlank()) {
                    contact.setTpoStatus(ContactStatus.valueOf(request.getTpoStatus()));
                } else {
                    contact.setTpoStatus(ContactStatus.ACTIVE);
                }
                
                contact.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
                
                contactsToInsert.add(contact);
                
            } catch (Exception e) {
                errors.add(new BulkInsertError(identifier, "Validation error: " + e.getMessage()));
            }
        }
        
        // Phase 2: If ANY errors exist, rollback and return errors (all-or-nothing)
        if (!errors.isEmpty()) {
            return new BulkInsertResponse<>(new ArrayList<>(), errors);
        }
        
        // Phase 3: Insert all records (within transaction, will auto-rollback on exception)
        List<InstituteContact> savedContacts = contactRepository.saveAll(contactsToInsert);
        List<InstituteContactResponse> responses = savedContacts.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        
        return new BulkInsertResponse<>(responses, new ArrayList<>());
    }
}
