package com.kanini.springer.repository.Drive;

import com.kanini.springer.entity.Drive.Drive;
import com.kanini.springer.entity.enums.Enums.DriveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriveRepository extends JpaRepository<Drive, Long> {
    List<Drive> findByStatus(DriveStatus status);
    List<Drive> findByCycleCycleId(Long cycleId);
    List<Drive> findByStatusOrderByStartDateDesc(DriveStatus status);
}
