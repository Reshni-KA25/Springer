package com.kanini.springer.repository.Drive;

import com.kanini.springer.entity.Drive.DriveRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriveRoundRepository extends JpaRepository<DriveRound, Integer> {
    List<DriveRound> findByDriveDriveId(Long driveId);
    void deleteByDriveDriveId(Long driveId);
}
