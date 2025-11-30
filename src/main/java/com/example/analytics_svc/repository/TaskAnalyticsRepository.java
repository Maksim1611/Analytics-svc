package com.example.analytics_svc.repository;

import com.example.analytics_svc.model.TaskAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskAnalyticsRepository extends JpaRepository<TaskAnalytics, UUID> {


    Optional<TaskAnalytics> findByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);
}
