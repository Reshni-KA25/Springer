package com.kanini.springer.repository.Drive;

import com.kanini.springer.entity.Drive.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByDriveDriveId(Long driveId);
    List<Application> findByCandidateCandidateId(Long candidateId);
    Optional<Application> findByRegistrationCode(String registrationCode);
}
