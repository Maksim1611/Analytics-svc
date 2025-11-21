package com.example.analytics_svc.repository;

import com.example.analytics_svc.model.ProjectSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectSnapshotRepository extends JpaRepository<ProjectSnapshot, UUID> {

    void deleteAllByUserId(UUID userId);

    List<ProjectSnapshot> findAllByUserIdAndDeletedFalse(UUID userId);

    List<ProjectSnapshot> findAllByUserId(UUID userId);
}
