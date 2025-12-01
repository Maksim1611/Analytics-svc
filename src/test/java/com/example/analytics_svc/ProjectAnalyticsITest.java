package com.example.analytics_svc;

import com.example.analytics_svc.model.ProjectAnalytics;
import com.example.analytics_svc.model.ProjectSnapshot;
import com.example.analytics_svc.repository.ProjectAnalyticsRepository;
import com.example.analytics_svc.repository.ProjectSnapshotRepository;
import com.example.analytics_svc.service.ProjectService;
import com.example.analytics_svc.web.dto.ProjectAnalyticsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ProjectAnalyticsITest {

    @Autowired
    private ProjectService service;

    @Autowired
    private ProjectSnapshotRepository snapshotRepository;

    @Autowired
    private ProjectAnalyticsRepository analyticsRepository;


    @Test
    void testUpsertProjects_createsSnapshotsAndAnalytics() {

        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        ProjectAnalyticsRequest request = ProjectAnalyticsRequest.builder()
                .userId(userId)
                .projectId(projectId)
                .createdOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .status("ACTIVE")
                .completionPercentage(20)
                .deleted(false)
                .build();

        ProjectAnalytics analytics = service.upsertProjects(List.of(request), userId);

        List<ProjectSnapshot> snapshots = snapshotRepository.findAllByUserId(userId);
        assertThat(snapshots.size()).isEqualTo(1);

        ProjectSnapshot snap = snapshots.get(0);
        assertThat(snap.getProjectId()).isEqualTo(projectId);
        assertThat(snap.getUserId()).isEqualTo(userId);
        assertThat(snap.getStatus()).isEqualTo("ACTIVE");
        assertThat(snap.getCompletionPercentage()).isEqualTo(20);


        Optional<ProjectAnalytics> analyticsOptional = analyticsRepository.findByUserId(userId);
        assertThat(analyticsOptional).isPresent();

        ProjectAnalytics result = analyticsOptional.get();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTotalProjects()).isEqualTo(1);
        assertThat(result.getActiveProjects()).isEqualTo(1);
        assertThat(result.getCompletedProjects()).isZero();
    }


    @Test
    void testUpsertProjects_updatesInsteadOfCreatingNewAnalytics() {

        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        ProjectAnalyticsRequest request1 = ProjectAnalyticsRequest.builder()
                .userId(userId)
                .projectId(projectId)
                .createdOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(5))
                .status("IN_PROGRESS")
                .completionPercentage(10)
                .deleted(false)
                .build();

        ProjectAnalytics first = service.upsertProjects(List.of(request1), userId);

        ProjectAnalyticsRequest request2 = ProjectAnalyticsRequest.builder()
                .userId(userId)
                .projectId(projectId)
                .createdOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(10))
                .status("COMPLETED")
                .completionPercentage(100)
                .deleted(false)
                .build();

        ProjectAnalytics second = service.upsertProjects(List.of(request2), userId);

        List<ProjectSnapshot> snapshots = snapshotRepository.findAllByUserId(userId);
        assertThat(snapshots.size()).isEqualTo(1);

        Optional<ProjectAnalytics> analyticsOptional = analyticsRepository.findByUserId(userId);
        assertThat(analyticsOptional).isPresent();

        ProjectAnalytics analytics = analyticsOptional.get();

        assertThat(analytics.getCompletedProjects()).isEqualTo(1);
        assertThat(analytics.getActiveProjects()).isEqualTo(0);

        assertThat(second.getId()).isEqualTo(first.getId());
    }
}
