package com.kanini.springer.mapper.Hiring;

import com.kanini.springer.dto.Hiring.InstituteWithTPOsResponse;
import com.kanini.springer.dto.Hiring.InstituteWithTPOsResponse.TPODetails;
import com.kanini.springer.entity.HiringReq.Institute;
import com.kanini.springer.entity.HiringReq.InstituteContact;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InstituteWithTPOsMapper {
    
    public InstituteWithTPOsResponse toResponse(Institute institute, List<InstituteContact> contacts) {
        InstituteWithTPOsResponse response = new InstituteWithTPOsResponse();
        
        response.setInstituteId(institute.getInstituteId());
        response.setInstituteName(institute.getInstituteName());
        response.setInstituteTier(institute.getInstituteTier() != null ? institute.getInstituteTier().toString() : null);
        response.setLocation(institute.getLocation());
        response.setState(institute.getState());
        response.setCity(institute.getCity());
        response.setIsActive(institute.getIsActive());
        response.setCreatedAt(institute.getCreatedAt());
        
        // Map TPO contacts to TPODetails
        List<TPODetails> tpoDetailsList = contacts.stream()
                .map(this::mapToTPODetails)
                .collect(Collectors.toList());
        
        response.setTpoDetails(tpoDetailsList);
        
        return response;
    }
    
    private TPODetails mapToTPODetails(InstituteContact contact) {
        TPODetails tpoDetails = new TPODetails();
        tpoDetails.setTpoId(contact.getTpoId());
        tpoDetails.setTpoName(contact.getTpoName());
        tpoDetails.setTpoEmail(contact.getTpoEmail());
        tpoDetails.setTpoMobile(contact.getTpoMobile());
        tpoDetails.setTpoStatus(contact.getTpoStatus() != null ? contact.getTpoStatus().toString() : null);
        tpoDetails.setIsPrimary(contact.getIsPrimary());
        tpoDetails.setCreatedAt(contact.getCreatedAt());
        return tpoDetails;
    }
}
