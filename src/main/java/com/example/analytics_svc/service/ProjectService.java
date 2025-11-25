package com.example.analytics_svc.service;
import com.example.analytics_svc.model.ProjectAnalytics;
import com.example.analytics_svc.model.ProjectSnapshot;
import com.example.analytics_svc.repository.ProjectAnalyticsRepository;
import com.example.analytics_svc.repository.ProjectSnapshotRepository;
import com.example.analytics_svc.web.dto.ProjectAnalyticsRequest;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectSnapshotRepository snapshotRepository;
    private final ProjectAnalyticsRepository projectAnalyticsRepository;
    private final EntityManager entityManager;

    public ProjectService(ProjectSnapshotRepository snapshotRepository, ProjectAnalyticsRepository projectAnalyticsRepository, EntityManager entityManager) {
        this.snapshotRepository = snapshotRepository;
        this.projectAnalyticsRepository = projectAnalyticsRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public ProjectAnalytics upsertProjects(List<ProjectAnalyticsRequest> requests, UUID userId) {

        if (requests == null || requests.isEmpty()) {
            return emptyAnalytics(userId);
        }

        snapshotRepository.deleteAllByUserId(userId);
        snapshotRepository.flush();
        entityManager.clear();

        for (ProjectAnalyticsRequest request : requests) {
            ProjectSnapshot snap = ProjectSnapshot.builder().projectId(request.getProjectId()).build();

            snap.setUserId(request.getUserId());
            snap.setStatus(request.getStatus());
            snap.setCompletionPercentage(request.getCompletionPercentage());
            snap.setCreatedOn(request.getCreatedOn());
            snap.setDueDate(request.getDueDate());
            snap.setDeleted(request.isDeleted());

            snapshotRepository.save(snap);
        }

        ProjectAnalytics projectAnalytics = getProjectAnalytics(snapshotRepository.findAllByUserIdAndDeletedFalse(userId), snapshotRepository.findAllByUserId(userId), userId);
        projectAnalytics.setUserId(userId);

        projectAnalyticsRepository.findByUserId(userId)
                .ifPresent(project -> projectAnalytics.setId(project.getId()));

        return projectAnalyticsRepository.save(projectAnalytics);
    }

    private ProjectAnalytics getProjectAnalytics(List<ProjectSnapshot> projects, List<ProjectSnapshot> lifetime, UUID userId) {

        if (projects.isEmpty()) {
            return emptyAnalytics(userId);
        }

        return ProjectAnalytics.builder()
                .userId(userId)
                .totalProjects(projects.size())
                .activeProjects(getActiveProjects(projects))
                .completedProjects(getCompletedProjects(projects))
                .averageProgress(getAverageProgress(projects))
                .overdueProjects(getOverdueProjects(projects))
                .totalProjectsLifetime(lifetime.size())
                .completedProjectsLifetime(getCompletedProjects(lifetime))
                .abandonedProjectsLifetime(getAbandonedLifetimeProjects(lifetime).size())
                .averageProjectDurationLifetime(getAverageProjectDurationLifetime(lifetime))
                .projectCompletionRateLifetime(getCompletionRateLifetime(lifetime))
                .build();
    }

    private double getCompletionRateLifetime(List<ProjectSnapshot> lifetime) {
        int completed = getCompletedProjects(lifetime);
        int projects = lifetime.size();

        if (projects == 0) {
            return 0;
        }

        return ((double)completed / projects) * 100.0;
    }

    private long getAverageProjectDurationLifetime(List<ProjectSnapshot> lifetime) {
        if (lifetime == null || lifetime.isEmpty()) {
            return 0;
        }

        List<Long> durationBetweenInDays = new ArrayList<>();

        for (ProjectSnapshot project : lifetime) {
            LocalDateTime completedOn = project.getCompletedOn();
            if (completedOn == null) {
                continue;
            }

            Duration duration = Duration.between(project.getCreatedOn(), completedOn);
            durationBetweenInDays.add(duration.toDays());
        }

        if (durationBetweenInDays.isEmpty()) {
            return 0;
        }

        long sum = durationBetweenInDays.stream().mapToLong(Long::longValue).sum();

        return sum / durationBetweenInDays.size();
    }

    private List<ProjectSnapshot> getAbandonedLifetimeProjects(List<ProjectSnapshot> lifetime) {
        return lifetime.stream().filter(p -> p.getStatus().equals("OVERDUE") && p.isDeleted()).toList();
    }

    private int getOverdueProjects(List<ProjectSnapshot> projects) {
        return (int) projects.stream().filter(p -> p.getStatus().equals("OVERDUE")).count();
    }

    private int getAverageProgress(List<ProjectSnapshot> projects) {
        if (projects.isEmpty()) {
            return 0;
        }

        int sum = projects.stream().mapToInt(ProjectSnapshot::getCompletionPercentage).sum();

        if (sum == 0) {
            return 0;
        }

        return Math.round((float) sum / projects.size());
    }

    private int getCompletedProjects(List<ProjectSnapshot> projects) {
        return (int) projects.stream().filter(p -> p.getStatus().equals("COMPLETED")).count();
    }

    private int getActiveProjects(List<ProjectSnapshot> projects) {
        return (int) projects.stream().filter(p -> p.getStatus().equals("ACTIVE")).count();
    }

    public ProjectAnalytics emptyAnalytics(UUID userId) {
        return ProjectAnalytics.builder()
                .userId(userId)
                .totalProjects(0)
                .activeProjects(0)
                .completedProjects(0)
                .averageProgress(0)
                .overdueProjects(0)
                .totalProjectsLifetime(0)
                .completedProjectsLifetime(0)
                .abandonedProjectsLifetime(0)
                .averageProjectDurationLifetime(0L)
                .projectCompletionRateLifetime(0D)
                .build();
    }
}
