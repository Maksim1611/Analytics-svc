package com.example.analytics_svc.repository;

import com.example.analytics_svc.model.ProjectAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectAnalyticsRepository extends JpaRepository<ProjectAnalytics, UUID> {
    Optional<ProjectAnalytics> findByUserId(UUID userId);
}
