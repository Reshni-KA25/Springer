package com.kanini.springer.service.Common.impl;

import com.kanini.springer.dto.Common.FieldChangeDTO;
import com.kanini.springer.dto.Common.ManualOverrideRequest;
import com.kanini.springer.dto.Common.ManualOverrideResponse;
import com.kanini.springer.entity.HiringReq.User;
import com.kanini.springer.entity.utils.ManualOverride;
import com.kanini.springer.entity.enums.Enums.OverrideEntityType;
import com.kanini.springer.mapper.Common.ManualOverrideMapper;
import com.kanini.springer.repository.Hiring.UserRepository;
import com.kanini.springer.repository.Common.ManualOverrideRepository;
import com.kanini.springer.service.Common.IOverrideService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OverrideServiceImpl implements IOverrideService {
    
    private final ManualOverrideRepository overrideRepository;
    private final UserRepository userRepository;
    private final ManualOverrideMapper mapper;
    
    @Override
    @Transactional
    public ManualOverrideResponse logOverride(ManualOverrideRequest request) {
        // Validate request
        if (request.getEntityType() == null || request.getEntityType().isBlank()) {
            throw new RuntimeException("Entity type is required");
        }
        if (request.getEntityId() == null) {
            throw new RuntimeException("Entity ID is required");
        }
        if (request.getOverrideReason() == null || request.getOverrideReason().isBlank()) {
            throw new RuntimeException("Override reason is required");
        }
        if (request.getCreatedBy() == null) {
            throw new RuntimeException("Created by user ID is required");
        }
        if (request.getChanges() == null || request.getChanges().isEmpty()) {
            throw new RuntimeException("Changes are required");
        }
        
        // Validate and fetch user
        User user = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getCreatedBy()));
        
        // Validate entity type
        OverrideEntityType entityType;
        try {
            entityType = OverrideEntityType.valueOf(request.getEntityType());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid entity type: " + request.getEntityType());
        }
        
        // Create ManualOverride entity
        ManualOverride override = new ManualOverride();
        override.setEntityType(entityType);
        override.setEntityId(request.getEntityId());
        override.setOverrideReason(request.getOverrideReason());
        override.setCreatedBy(user);
        
        // Convert FieldChangeDTO to FieldChange
        List<ManualOverride.FieldChange> fieldChanges = new ArrayList<>();
        for (FieldChangeDTO dto : request.getChanges()) {
            ManualOverride.FieldChange fieldChange = new ManualOverride.FieldChange(
                    dto.getField(),
                    dto.getOld(),
                    dto.getNewValue()
            );
            fieldChanges.add(fieldChange);
        }
        override.setChanges(fieldChanges);
        
        // Save within transaction
        ManualOverride savedOverride = overrideRepository.save(override);
        
        return mapper.toResponse(savedOverride);
    }
    
    @Override
    public List<ManualOverrideResponse> getAllOverrides() {
        List<ManualOverride> overrides = overrideRepository.findAllWithUser();
        return mapper.toResponseList(overrides);
    }
    
    @Override
    public List<ManualOverrideResponse> getOverridesByDate(LocalDate fromDate) {
        if (fromDate == null) {
            throw new RuntimeException("From date is required");
        }
        
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        List<ManualOverride> overrides = overrideRepository.findByCreatedAtGreaterThanEqualWithUser(fromDateTime);
        return mapper.toResponseList(overrides);
    }
    
    @Override
    public List<ManualOverrideResponse> getOverridesByEntityType(String entityType) {
        if (entityType == null || entityType.isBlank()) {
            throw new RuntimeException("Entity type is required");
        }
        
        OverrideEntityType overrideEntityType;
        try {
            overrideEntityType = OverrideEntityType.valueOf(entityType);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid entity type: " + entityType);
        }
        
        List<ManualOverride> overrides = overrideRepository.findByEntityTypeWithUser(overrideEntityType);
        return mapper.toResponseList(overrides);
    }
    
    @Override
    public List<ManualOverrideResponse> getOverridesByUserId(Long userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        List<ManualOverride> overrides = overrideRepository.findByCreatedByUserIdWithUser(userId);
        return mapper.toResponseList(overrides);
    }
    
    @Override
    public <T> List<FieldChangeDTO> detectChanges(T oldEntity, T newEntity) {
        List<FieldChangeDTO> changes = new ArrayList<>();
        
        if (oldEntity == null || newEntity == null) {
            throw new RuntimeException("Both old and new entity must not be null");
        }
        
        if (!oldEntity.getClass().equals(newEntity.getClass())) {
            throw new RuntimeException("Old and new entity must be of the same type");
        }
        
        Class<?> entityClass = oldEntity.getClass();
        Field[] fields = entityClass.getDeclaredFields();
        
        for (Field field : fields) {
            // Skip fields that are not meant to be compared
            if (isSkippableField(field)) {
                continue;
            }
            
            field.setAccessible(true);
            
            try {
                Object oldValue = field.get(oldEntity);
                Object newValue = field.get(newEntity);
                
                // Check if values are different
                if (!Objects.equals(oldValue, newValue)) {
                    FieldChangeDTO change = new FieldChangeDTO();
                    change.setField(field.getName());
                    change.setOld(oldValue);
                    change.setNewValue(newValue);
                    changes.add(change);
                }
            } catch (IllegalAccessException e) {
                // Log and continue
                System.err.println("Error accessing field: " + field.getName());
            }
        }
        
        return changes;
    }
    
    /**
     * Determine if a field should be skipped during change detection
     */
    private boolean isSkippableField(Field field) {
        String fieldName = field.getName();
        
        // Skip common metadata fields
        if (fieldName.equals("createdAt") || 
            fieldName.equals("updatedAt") || 
            fieldName.equals("createdBy") || 
            fieldName.equals("updatedBy") ||
            fieldName.startsWith("$")) { // Skip synthetic fields
            return true;
        }
        
        // Skip collections and complex relationships (to avoid lazy loading issues)
        if (java.util.Collection.class.isAssignableFrom(field.getType())) {
            return true;
        }
        
        return false;
    }
}

