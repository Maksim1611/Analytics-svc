package com.example.analytics_svc.web;

import com.example.analytics_svc.domain.AnalyticsNotFound;
import com.example.analytics_svc.model.ProjectAnalytics;
import com.example.analytics_svc.repository.ProjectAnalyticsRepository;
import com.example.analytics_svc.repository.ProjectSnapshotRepository;
import com.example.analytics_svc.service.ProjectService;
import com.example.analytics_svc.web.dto.ProjectAnalyticsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectSnapshotController {

    private final ProjectService projectService;
    private final ProjectSnapshotRepository snapshotRepository;
    private final ProjectAnalyticsRepository analyticsRepository;

    public ProjectSnapshotController(ProjectService projectService, ProjectSnapshotRepository snapshotRepository, ProjectAnalyticsRepository analyticsRepository) {
        this.projectService = projectService;
        this.snapshotRepository = snapshotRepository;
        this.analyticsRepository = analyticsRepository;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ProjectAnalytics> updateAnalytics(@RequestBody List<ProjectAnalyticsRequest> requests, @PathVariable UUID userId) {

        if (requests == null || requests.isEmpty()) {
            snapshotRepository.deleteAllByUserId(userId);
            return ResponseEntity.ok().build();
        }

        ProjectAnalytics projectAnalytics = projectService.upsertProjects(requests, userId);
        return ResponseEntity.ok(projectAnalytics);
    }

    @GetMapping("/{userId}")
    public ProjectAnalytics getProjectAnalytics(@PathVariable UUID userId) {
        try {
            return analyticsRepository.findByUserId(userId).orElseThrow(() ->
                    new AnalyticsNotFound("Project analytics not found for user: [%s]".formatted(userId)));
        } catch (AnalyticsNotFound e) {
            ProjectAnalytics projectAnalytics = projectService.emptyAnalytics(userId);
            return analyticsRepository.save(projectAnalytics);
        }
    }
}
