package com.example.analytics_svc.repository;

import com.example.analytics_svc.model.TaskSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskSnapshotRepository extends JpaRepository<TaskSnapshot, UUID> {

    List<TaskSnapshot> findAllByUserIdAndDeletedFalse(UUID userId);

    void deleteAllByUserId(UUID userId);

    List<TaskSnapshot> findAllByUserId(UUID userId);
}
