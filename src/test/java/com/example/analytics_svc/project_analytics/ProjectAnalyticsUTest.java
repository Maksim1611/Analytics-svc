package com.example.analytics_svc.project_analytics;

import com.example.analytics_svc.model.ProjectAnalytics;
import com.example.analytics_svc.model.ProjectSnapshot;
import com.example.analytics_svc.repository.ProjectAnalyticsRepository;
import com.example.analytics_svc.repository.ProjectSnapshotRepository;
import com.example.analytics_svc.service.ProjectService;
import com.example.analytics_svc.web.dto.ProjectAnalyticsRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectAnalyticsUTest {

    @Mock
    private ProjectSnapshotRepository snapshotRepository;
    @Mock
    private ProjectAnalyticsRepository projectAnalyticsRepository;
    @Mock
    private EntityManager entityManager;

    @Spy
    @InjectMocks
    private ProjectService projectService;

    @Test
    void whenGetEmptyAnalytics_thenReturnAllZeroValues() {
        UUID userId = UUID.randomUUID();

        ProjectAnalytics result = projectService.emptyAnalytics(userId);

        assertEquals(userId, result.getUserId());
        assertEquals(0, result.getTotalProjects());
        assertEquals(0, result.getActiveProjects());
        assertEquals(0, result.getCompletedProjects());
        assertEquals(0, result.getAverageProgress());
        assertEquals(0, result.getOverdueProjects());

        assertEquals(0, result.getTotalProjectsLifetime());
        assertEquals(0, result.getCompletedProjectsLifetime());
        assertEquals(0, result.getAbandonedProjectsLifetime());
        assertEquals(0L, result.getAverageProjectDurationLifetime());
        assertEquals(0D, result.getProjectCompletionRateLifetime());
    }

    @Test
    void whenUpsert_andRequestsAreNull_thenReturnsEmptyAnalytics() {
        UUID userId = UUID.randomUUID();
        ProjectAnalytics empty = new ProjectAnalytics();

        doReturn(empty).when(projectService).emptyAnalytics(userId);

        ProjectAnalytics result = projectService.upsertProjects(null, userId);

        assertSame(empty, result);
        verify(snapshotRepository, never()).deleteAllByUserId(any());
        verify(projectAnalyticsRepository, never()).save(any());
    }

    @Test
    void whenUpsert_andEmptyList_thenReturnsEmptyAnalytics() {
        UUID userId = UUID.randomUUID();
        ProjectAnalytics empty = new ProjectAnalytics();

        doReturn(empty).when(projectService).emptyAnalytics(userId);

        ProjectAnalytics result = projectService.upsertProjects(Collections.emptyList(), userId);

        assertSame(empty, result);
        verify(snapshotRepository, never()).deleteAllByUserId(any());
    }

    @Test
    void whenUpsert_andAllValidRequests_thenSaveSnapshotsAndAnalytics() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        ProjectAnalyticsRequest request = request(projectId);
        List<ProjectAnalyticsRequest> requests = List.of(request);

        ProjectAnalytics analytics = new ProjectAnalytics();
        doReturn(analytics).when(projectService)
                .getProjectAnalytics(anyList(), anyList(), eq(userId));

        when(projectAnalyticsRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        ProjectAnalytics saved = new ProjectAnalytics();
        when(projectAnalyticsRepository.save(any())).thenReturn(saved);

        ProjectAnalytics result = projectService.upsertProjects(requests, userId);

        verify(snapshotRepository).deleteAllByUserId(userId);
        verify(snapshotRepository).save(any(ProjectSnapshot.class));
        verify(projectAnalyticsRepository).save(analytics);

        assertSame(saved, result);
    }

    @Test
    void whenUpsert_andExistingAnalytics_thenSetIdAndSaves() {
        UUID userId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();

        ProjectAnalytics existing = new ProjectAnalytics();
        existing.setId(existingId);

        when(projectAnalyticsRepository.findByUserId(userId))
                .thenReturn(Optional.of(existing));

        ProjectAnalytics analytics = new ProjectAnalytics();
        doReturn(analytics).when(projectService)
                .getProjectAnalytics(any(), any(), eq(userId));

        when(projectAnalyticsRepository.save(any())).thenReturn(analytics);

        ProjectAnalytics result = projectService.upsertProjects(List.of(request(UUID.randomUUID())), userId);

        assertEquals(existingId, analytics.getId());
        verify(projectAnalyticsRepository).save(analytics);
        assertSame(analytics, result);
    }


    @Test
    void whenGetProjectAnalytics_andEmptyProjects_thenReturnsEmptyAnalytics() {
        UUID userId = UUID.randomUUID();
        ProjectAnalytics empty = new ProjectAnalytics();

        doReturn(empty)
                .when(projectService)
                .emptyAnalytics(userId);

        ProjectAnalytics result = projectService.getProjectAnalytics(Collections.emptyList(), Collections.emptyList(), userId);

        assertSame(empty, result);
    }

    @Test
    void getProjectAnalytics_validProjects_populatesAllFieldsCorrectly() {
        UUID userId = UUID.randomUUID();

        List<ProjectSnapshot> projects = List.of(
                new ProjectSnapshot(),
                new ProjectSnapshot(),
                new ProjectSnapshot());

        List<ProjectSnapshot> lifetime = List.of(
                new ProjectSnapshot(),
                new ProjectSnapshot());

        doReturn(2).when(projectService).getActiveProjects(projects);
        doReturn(1).when(projectService).getCompletedProjects(projects);
        doReturn(70).when(projectService).getAverageProgress(projects);
        doReturn(3).when(projectService).getOverdueProjects(projects);

        doReturn(2)
                .when(projectService).getCompletedProjects(lifetime);

        doReturn(List.of(new ProjectSnapshot()))
                .when(projectService).getAbandonedLifetimeProjects(lifetime);

        doReturn(48L)
                .when(projectService).getAverageProjectDurationLifetime(lifetime);

        doReturn(55.0)
                .when(projectService).getCompletionRateLifetime(lifetime);

        ProjectAnalytics result = projectService.getProjectAnalytics(projects, lifetime, userId);

        assertEquals(userId, result.getUserId());
        assertEquals(3, result.getTotalProjects());
        assertEquals(2, result.getActiveProjects());
        assertEquals(1, result.getCompletedProjects());
        assertEquals(70, result.getAverageProgress());
        assertEquals(3, result.getOverdueProjects());

        assertEquals(2, result.getTotalProjectsLifetime());
        assertEquals(2, result.getCompletedProjectsLifetime());
        assertEquals(1, result.getAbandonedProjectsLifetime());
        assertEquals(48L, result.getAverageProjectDurationLifetime());
        assertEquals(55.0, result.getProjectCompletionRateLifetime());
    }

    @Test
    void whenGetAverageDuration_nullList_returnsZero() {
        long result = projectService.getAverageProjectDurationLifetime(null);
        assertEquals(0L, result);
    }

    @Test
    void whenGetAverageDuration_andEmptyList_thenReturnsZero() {
        long result = projectService.getAverageProjectDurationLifetime(Collections.emptyList());
        assertEquals(0L, result);
    }

    @Test
    void whenGetAverageDuration_andAllNullCompleted_thenReturnsZero() {
        List<ProjectSnapshot> items = List.of(
                snap(LocalDateTime.now(), null),
                snap(LocalDateTime.now().minusDays(1), null)
        );

        long result = projectService.getAverageProjectDurationLifetime(items);

        assertEquals(0L, result);
    }

    @Test
    void whenGetAverageDuration_andListHasCompletedNullAndNotNull_thenIgnoresNullCompleted() {
        LocalDateTime now = LocalDateTime.now();

        List<ProjectSnapshot> items = List.of(
                snap(now.minusDays(4), now),
                snap(now.minusDays(2), now),
                snap(now.minusDays(10), null)
        );

        long result = projectService.getAverageProjectDurationLifetime(items);

        assertEquals(3L, result);
    }

    @Test
    void whenGetAverageDuration_andAllValid_thenReturnsCorrectAverage() {
        LocalDateTime now = LocalDateTime.now();

        List<ProjectSnapshot> items = List.of(
                snap(now.minusDays(10), now.minusDays(6)),
                snap(now.minusDays(5),  now.minusDays(1))
        );

        long result = projectService.getAverageProjectDurationLifetime(items);

        assertEquals(4L, result);
    }

    @Test
    void whenGetCompletionRate_addEmptyList_thenReturnsZero() {
        double result = projectService.getCompletionRateLifetime(Collections.emptyList());
        assertEquals(0.0, result);
    }

    @Test
    void wheGetCompletionRate_andThereAreNoCompletedProjects_thenReturnsZeroPercent() {
        List<ProjectSnapshot> lifetime = List.of(
                new ProjectSnapshot(),
                new ProjectSnapshot(),
                new ProjectSnapshot()
        );

        doReturn(0).when(projectService).getCompletedProjects(lifetime);

        double result = projectService.getCompletionRateLifetime(lifetime);

        assertEquals(0.0, result);
    }


    @Test
    void whenGetCompletionRate_andProjectsAreMixed_thenReturnsCorrectPercentage() {
        List<ProjectSnapshot> lifetime = List.of(
                new ProjectSnapshot(),
                new ProjectSnapshot(),
                new ProjectSnapshot(),
                new ProjectSnapshot()
        );

        doReturn(2).when(projectService).getCompletedProjects(lifetime);

        double result = projectService.getCompletionRateLifetime(lifetime);

        assertEquals(50.0, result);
    }

    @Test
    void whenGetAverageProgress_andEmptyList_thenReturnsZero() {
        int result = projectService.getAverageProgress(Collections.emptyList());
        assertEquals(0, result);
    }

    @Test
    void whenGetAverageProgress_andAllAreZeroProgress_thenReturnsZero() {
        List<ProjectSnapshot> list = List.of(
                snap(0),
                snap(0),
                snap(0)
        );

        int result = projectService.getAverageProgress(list);

        assertEquals(0, result);
    }

    @Test
    void whenGetAverageProgress_andValuesAreMixed_thenReturnsRoundedAverage() {
        List<ProjectSnapshot> list = List.of(
                snap(50),
                snap(50),
                snap(50)
        );

        int result = projectService.getAverageProgress(list);

        assertEquals(50, result);
    }

    @Test
    void whenGetAverageProgress_andFullCompletion_thenReturns100() {
        List<ProjectSnapshot> list = List.of(
                snap(100),
                snap(100),
                snap(100)
        );

        int result = projectService.getAverageProgress(list);

        assertEquals(100, result);
    }

    @Test
    void whenGetCompletedProjects_andProjectsAreEmpty_thenReturn0() {
        List<ProjectSnapshot> emptyList = Collections.emptyList();
        doReturn(0).when(projectService).getCompletedProjects(emptyList);

        int result = projectService.getCompletedProjects(emptyList);
        assertEquals(0, result);
    }

    @Test
    void whenGetCompletedProjects_andProjectsNotEmptyAndHas2CompletedProjects_thenReturn2() {
        List<ProjectSnapshot> list = List.of(
                ProjectSnapshot.builder().status("COMPLETED").build(),
                ProjectSnapshot.builder().status("COMPLETED").build()
        );

        doReturn(2).when(projectService).getCompletedProjects(list);

        int result = projectService.getCompletedProjects(list);
        assertEquals(2, result);
    }

    @Test
    void whenGetOverdueProjects_andProjectsAreEmpty_thenReturn0() {
        List<ProjectSnapshot> emptyList = Collections.emptyList();

        int result = projectService.getOverdueProjects(emptyList);
        assertEquals(0, result);
    }

    @Test
    void whenGetOverdueProjects_andProjectsNotEmptyAndHas2OverdueProjects_thenReturn2() {
        List<ProjectSnapshot> list = List.of(
                ProjectSnapshot.builder().status("OVERDUE").build(),
                ProjectSnapshot.builder().status("OVERDUE").build()
        );

        int result = projectService.getOverdueProjects(list);
        assertEquals(2, result);
    }

    @Test
    void whenGetAbandonedLifetimeProjects_andProjectsAreEmpty_thenReturn0() {
        List<ProjectSnapshot> emptyList = Collections.emptyList();

        List<ProjectSnapshot> result = projectService.getAbandonedLifetimeProjects(emptyList);
        assertEquals(emptyList, result);
    }

    @Test
    void whenGetAbandonedLifetimeProjects_andProjectsNotEmptyAndHas2AbandonedProjects_thenReturn2() {
        List<ProjectSnapshot> list = List.of(
                ProjectSnapshot.builder().status("OVERDUE").deleted(true).build(),
                ProjectSnapshot.builder().status("OVERDUE").deleted(true).build()
        );

        List<ProjectSnapshot> result = projectService.getAbandonedLifetimeProjects(list);
        assertEquals(list, result);
    }

    @Test
    void whenGetActiveProjects_andProjectsAreEmpty_thenReturn0() {
        List<ProjectSnapshot> emptyList = Collections.emptyList();

        int result = projectService.getOverdueProjects(emptyList);
        assertEquals(0, result);
    }

    @Test
    void whenGetActiveProjects_andProjectsNotEmptyAndHas2ActiveProjects_thenReturn2() {
        List<ProjectSnapshot> list = List.of(
                ProjectSnapshot.builder().status("ACTIVE").build(),
                ProjectSnapshot.builder().status("ACTIVE").build()
        );

        int result = projectService.getActiveProjects(list);
        assertEquals(2, result);
    }

    private ProjectAnalyticsRequest request(UUID id) {
        ProjectAnalyticsRequest request = ProjectAnalyticsRequest.builder().build();
        request.setProjectId(id);
        request.setUserId(id);
        request.setStatus("ACTIVE");
        request.setCompletionPercentage(50);
        request.setCreatedOn(LocalDateTime.now());
        request.setDueDate(LocalDateTime.now().plusDays(3));
        request.setDeleted(false);
        return request;
    }

    private ProjectSnapshot snap(LocalDateTime created, LocalDateTime completed) {
        ProjectSnapshot p = new ProjectSnapshot();
        p.setCreatedOn(created);
        p.setCompletedOn(completed);
        return p;
    }

    private ProjectSnapshot snap(int percent) {
        ProjectSnapshot p = new ProjectSnapshot();
        p.setCompletionPercentage(percent);
        return p;
    }
}
