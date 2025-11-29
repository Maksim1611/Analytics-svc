package com.example.analytics_svc.task_analytics;

import com.example.analytics_svc.model.TaskAnalytics;
import com.example.analytics_svc.model.TaskSnapshot;
import com.example.analytics_svc.repository.TaskAnalyticsRepository;
import com.example.analytics_svc.repository.TaskSnapshotRepository;
import com.example.analytics_svc.service.TaskService;
import com.example.analytics_svc.web.dto.TaskAnalyticsRequest;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskAnalyticsUTest {

    @Mock
    private TaskSnapshotRepository snapshotRepository;
    @Mock
    private TaskAnalyticsRepository taskAnalyticsRepository;
    @Mock
    private EntityManager entityManager;

    @Spy
    @InjectMocks
    private TaskService taskService;

    @Test
    void whenUpsert_andTasksAreNull_thenReturnsEmptyAnalyticsAndDoesNotTouchDatabase() {
        UUID userId = UUID.randomUUID();
        TaskAnalytics empty = new TaskAnalytics();

        doReturn(empty).when(taskService).emptyAnalytics(userId);

        TaskAnalytics result = taskService.upsertTasks(Collections.emptyList(), userId);

        assertSame(empty, result);
        verify(snapshotRepository, never()).deleteAllByUserId(any());
        verify(snapshotRepository, never()).save(any());
        verify(taskAnalyticsRepository, never()).save(any());
    }

    @Test
    void whenUpsert_andTasksAreEmpty_thenReturnsEmptyAnalyticsAndDoesNotTouchDatabase() {
        UUID userId = UUID.randomUUID();
        TaskAnalytics empty = new TaskAnalytics();

        doReturn(empty).when(taskService).emptyAnalytics(userId);

        TaskAnalytics result = taskService.upsertTasks(Collections.emptyList(), userId);

        assertSame(empty, result);
        verify(snapshotRepository, never()).deleteAllByUserId(any());
        verify(snapshotRepository, never()).save(any());
        verify(taskAnalyticsRepository, never()).save(any());
    }

    @Test
    void whenUpsert_thenCallsDeleteSnapshotsOnce() {
        UUID userId = UUID.randomUUID();
        List<TaskAnalyticsRequest> tasks = List.of(TaskAnalyticsRequest.builder().build());

        doReturn(new TaskAnalytics()).when(taskService)
                .getTasksAnalytics(any(), any(), any());

        taskService.upsertTasks(tasks, userId);

        verify(snapshotRepository).deleteAllByUserId(userId);
    }

    @Test
    void whenUpsert_thenSavesSnapshotForEachTask() {
        UUID userId = UUID.randomUUID();

        TaskAnalyticsRequest t1 = TaskAnalyticsRequest.builder().build();
        t1.setTaskId(UUID.randomUUID());

        TaskAnalyticsRequest t2 = TaskAnalyticsRequest.builder().build();
        t2.setTaskId(UUID.randomUUID());

        List<TaskAnalyticsRequest> tasks = List.of(t1, t2);

        doReturn(new TaskAnalytics()).when(taskService)
                .getTasksAnalytics(any(), any(), any());

        taskService.upsertTasks(tasks, userId);

        verify(snapshotRepository, times(2)).save(any(TaskSnapshot.class));
    }

    @Test
    void whenUpsert_thenCallsGetTasksAnalytics() {
        UUID userId = UUID.randomUUID();
        TaskAnalyticsRequest req = TaskAnalyticsRequest.builder().build();
        req.setTaskId(UUID.randomUUID());

        List<TaskAnalyticsRequest> tasks = List.of(req);

        TaskAnalytics analytics = new TaskAnalytics();
        doReturn(analytics).when(taskService)
                .getTasksAnalytics(any(), any(), any());

        taskService.upsertTasks(tasks, userId);

        verify(taskService).getTasksAnalytics(any(), any(), eq(userId));
    }

    @Test
    void whenUpsert_thenSavesAnalyticsAndReturnsValue() {
        UUID userId = UUID.randomUUID();

        TaskAnalyticsRequest req = TaskAnalyticsRequest.builder().build();
        req.setTaskId(UUID.randomUUID());

        List<TaskAnalyticsRequest> tasks = List.of(req);

        TaskAnalytics analytics = new TaskAnalytics();
        TaskAnalytics saved = new TaskAnalytics();

        doReturn(analytics).when(taskService)
                .getTasksAnalytics(any(), any(), any());

        when(taskAnalyticsRepository.save(analytics)).thenReturn(saved);

        TaskAnalytics result = taskService.upsertTasks(tasks, userId);

        verify(taskAnalyticsRepository).save(analytics);
        assertSame(saved, result);
    }

    @Test
    void whenGetTasksAnalytics_thenReturnsCorrectAnalyticsValues() {
        UUID userId = UUID.randomUUID();

        List<TaskSnapshot> current = List.of(new TaskSnapshot(), new TaskSnapshot());
        List<TaskSnapshot> lifetime = List.of(new TaskSnapshot());

        doReturn(List.of(new TaskSnapshot()))
                .when(taskService).getCompletedTasks(current);

        doReturn(1)
                .when(taskService).getInProgressTasks(current);

        doReturn(1)
                .when(taskService).getTodoTasks(current);

        doReturn(1)
                .when(taskService).getOverdueTasks(current);

        doReturn(50.0)
                .when(taskService).getCompletionRate(current);

        doReturn(123.0)
                .when(taskService).averageCompletionTime(current);

        doReturn(1)
                .when(taskService).getLowPriorityTasks(current);

        doReturn(1)
                .when(taskService).getMediumPriorityTasks(current);

        doReturn(1)
                .when(taskService).getHighPriorityTasks(current);

        doReturn(List.of(new TaskSnapshot()))
                .when(taskService).getCompletedTasks(lifetime);

        doReturn(List.of(new TaskSnapshot()))
                .when(taskService).getAbandonedLifetimeTasks(lifetime);

        doReturn(List.of(new TaskSnapshot()))
                .when(taskService).getLifetimeOverdueTasks(lifetime);

        doReturn(25.0)
                .when(taskService).averageCompletionTime(lifetime);

        doReturn(66.0)
                .when(taskService).getCompletionRate(lifetime);

        doReturn(13L)
                .when(taskService).getFastestCompletionTime(lifetime);

        TaskAnalytics result = taskService.getTasksAnalytics(current, lifetime, userId);

        assertEquals(userId, result.getUserId());
        assertEquals(2, result.getTotalTasks());
        assertEquals(1, result.getCompletedTasks());
        assertEquals(1, result.getInProgressTasks());
        assertEquals(1, result.getTodoTasks());
        assertEquals(1, result.getOverdueTasks());
        assertEquals(50.0, result.getCompletionRate());
        assertEquals(123.0, result.getAvgCompletionTime());
        assertEquals(1, result.getLowPriorityCount());
        assertEquals(1, result.getMediumPriorityCount());
        assertEquals(1, result.getHighPriorityCount());

        assertEquals(1, result.getLifetimeTotalTasks());
        assertEquals(1, result.getLifetimeCompletedTasks());
        assertEquals(1, result.getLifetimeAbandonedTasks());
        assertEquals(1, result.getLifetimeOverdueTasks());
        assertEquals(25.0, result.getLifetimeAverageCompletionTime());
        assertEquals(66, result.getLifetimeCompletionRate());
        assertEquals(13L, result.getFastestCompletionTime());
    }

    @Test
    void whenGetLowPriorityTasks_thenReturnsCorrectCount() {
        List<TaskSnapshot> tasks = List.of(
                snap("TODO", "LOW"),
                snap("COMPLETED", "HIGH"),
                snap("IN_PROGRESS", "LOW")
        );

        int count = taskService.getLowPriorityTasks(tasks);

        assertEquals(2, count);
    }

    @Test
    void whenGetMediumPriorityTasks_thenReturnsCorrectCount() {
        List<TaskSnapshot> tasks = List.of(
                snap("TODO", "MEDIUM"),
                snap("IN_PROGRESS", "LOW"),
                snap("COMPLETED", "MEDIUM")
        );

        int count = taskService.getMediumPriorityTasks(tasks);

        assertEquals(2, count);
    }

    @Test
    void whenGetHighPriorityTasks_thenReturnsCorrectCount() {
        List<TaskSnapshot> tasks = List.of(
                snap("TODO", "HIGH"),
                snap("COMPLETED", "HIGH"),
                snap("IN_PROGRESS", "LOW")
        );

        int count = taskService.getHighPriorityTasks(tasks);

        assertEquals(2, count);
    }

    @Test
    void whenGetTodoTasks_thenReturnsCorrectCount() {
        List<TaskSnapshot> tasks = List.of(
                snap("TODO", "LOW"),
                snap("COMPLETED", "HIGH"),
                snap("TODO", "MEDIUM")
        );

        int count = taskService.getTodoTasks(tasks);

        assertEquals(2, count);
    }

    @Test
    void whenGetCompletedTasks_thenReturnsOnlyCompletedTasks() {
        List<TaskSnapshot> tasks = List.of(
                snap("COMPLETED", "LOW"),
                snap("COMPLETED", "HIGH"),
                snap("TODO", "LOW")
        );

        List<TaskSnapshot> result = taskService.getCompletedTasks(tasks);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getStatus().equals("COMPLETED")));
    }


    @Test
    void whenGetInProgressTasks_thenReturnsCorrectCount() {
        List<TaskSnapshot> tasks = List.of(
                snap("IN_PROGRESS", "LOW"),
                snap("IN_PROGRESS", "HIGH"),
                snap("TODO", "LOW")
        );

        int count = taskService.getInProgressTasks(tasks);

        assertEquals(2, count);
    }


    @Test
    void whenGetOverdueTasks_thenReturnsCorrectCount() {
        List<TaskSnapshot> tasks = List.of(
                snap("OVERDUE", "HIGH"),
                snap("OVERDUE", "LOW"),
                snap("COMPLETED", "LOW")
        );

        int count = taskService.getOverdueTasks(tasks);

        assertEquals(2, count);
    }

    @Test
    void whenGetAverageCompletionTime_andServiceReturnsEmptyList_thenReturnsZero() {
        double result = taskService.averageCompletionTime(Collections.emptyList());
        assertEquals(0.0, result);
    }

    @Test
    void whenGetAverageCompletionTime_andAllNullCompleted_thenReturnsZero() {
        List<TaskSnapshot> tasks = List.of(
                snap(LocalDateTime.now(), null),
                snap(LocalDateTime.now().minusDays(1), null)
        );

        double result = taskService.averageCompletionTime(tasks);

        assertEquals(0.0, result);
    }

    @Test
    void whenGetAverageCompletionTime_andSomeNullCompleted_thenIgnoreIt() {
        LocalDateTime now = LocalDateTime.now();

        List<TaskSnapshot> tasks = List.of(
                snap(now.minusDays(4), now),
                snap(now.minusDays(2), now),
                snap(now.minusDays(7), null)
        );

        double result = taskService.averageCompletionTime(tasks);

        assertEquals(3.0, result);
    }

    @Test
    void whenGetAverageCompletionTime_andDllValidComputesCorrect_thenReturnAverage() {
        LocalDateTime now = LocalDateTime.now();

        List<TaskSnapshot> tasks = List.of(
                snap(now.minusDays(10), now.minusDays(5)),
                snap(now.minusDays(6),  now.minusDays(1))
        );

        double result = taskService.averageCompletionTime(tasks);

        assertEquals(5.0, result);
    }

    @Test
    void whenGetEmptyAnalytics_thenReturnsAnalyticsWithZeroValues() {
        UUID userId = UUID.randomUUID();

        TaskAnalytics result = taskService.emptyAnalytics(userId);

        assertEquals(userId, result.getUserId());
        assertEquals(0, result.getTotalTasks());
        assertEquals(0, result.getCompletedTasks());
        assertEquals(0, result.getInProgressTasks());
        assertEquals(0, result.getTodoTasks());
        assertEquals(0, result.getOverdueTasks());
        assertEquals(0, result.getCompletionRate());
        assertEquals(0, result.getAvgCompletionTime());
        assertEquals(0, result.getLowPriorityCount());
        assertEquals(0, result.getMediumPriorityCount());
        assertEquals(0, result.getHighPriorityCount());
        assertEquals(0, result.getLifetimeTotalTasks());
        assertEquals(0, result.getLifetimeCompletedTasks());
        assertEquals(0, result.getLifetimeAbandonedTasks());
        assertEquals(0, result.getLifetimeOverdueTasks());
        assertEquals(0, result.getLifetimeAverageCompletionTime());
        assertEquals(0, result.getLifetimeCompletionRate());
        assertEquals(0, result.getFastestCompletionTime());
    }

    @Test
    void whenGetCompletionRate_andIsEmptyList_thenReturnsZero() {
        double rate = taskService.getCompletionRate(Collections.emptyList());
        assertEquals(0.0, rate);
    }

    @Test
    void whenGetCompletionRate_andThereAreNoCompletedTasks_thenReturnsZero() {
        List<TaskSnapshot> tasks = List.of(
                snap("TODO"),
                snap("IN_PROGRESS"),
                snap("OVERDUE")
        );

        when(taskService.getCompletedTasks(tasks)).thenReturn(Collections.emptyList());

        double rate = taskService.getCompletionRate(tasks);

        assertEquals(0.0, rate);
    }

    @Test
    void whenGetCompletionRate_mixedTasks_thenComputesCorrectRate() {
        List<TaskSnapshot> tasks = List.of(
                snap("COMPLETED"),
                snap("COMPLETED"),
                snap("TODO"),
                snap("IN_PROGRESS")
        );

        when(taskService.getCompletedTasks(tasks)).thenReturn(List.of(
                snap("COMPLETED"),
                snap("COMPLETED")
        ));

        double rate = taskService.getCompletionRate(tasks);

        assertEquals(50.0, rate);
    }

    private TaskSnapshot snap(String status, String priority) {
        TaskSnapshot s = new TaskSnapshot();
        s.setStatus(status);
        s.setPriority(priority);
        return s;
    }

    @Test
    void whenGetFastestCompletion_andEmptyList_thenReturnsZero() {
        long result = taskService.getFastestCompletionTime(Collections.emptyList());
        assertEquals(0L, result);
    }

    @Test
    void whenGetFastestCompletion_andAllNullCompleted_thenReturnsZero() {
        List<TaskSnapshot> tasks = List.of(
                snap(LocalDateTime.now(), null),
                snap(LocalDateTime.now().minusDays(1), null));

        long result = taskService.getFastestCompletionTime(tasks);

        assertEquals(0L, result);
    }

    @Test
    void whenGetFastestCompletion_andAllValidDurations_thenReturnsMinimum() {
        LocalDateTime now = LocalDateTime.now();

        List<TaskSnapshot> tasks = List.of(
                snap(now.minusHours(10), now),
                snap(now.minusHours(3),  now),
                snap(now.minusHours(7),  now)
        );

        long result = taskService.getFastestCompletionTime(tasks);

        assertEquals(3L, result);
    }


    @Test
    void whenGetLifetimeOverdueTasks_thenFiltersCorrectly() {
        List<TaskSnapshot> tasks = List.of(
                snap("OVERDUE", true),
                snap("OVERDUE", false),
                snap("COMPLETED", true)
        );

        List<TaskSnapshot> result = taskService.getLifetimeOverdueTasks(tasks);

        assertEquals(1, result.size());
        assertEquals("OVERDUE", result.get(0).getStatus());
        assertTrue(result.get(0).isDeleted());
    }

    @Test
    void whenGetAbandonedLifetimeTasks_thenFiltersCorrectly() {
        List<TaskSnapshot> tasks = List.of(
                snap("TODO", true),
                snap("IN_PROGRESS", true),
                snap("COMPLETED", true),
                snap("OVERDUE", false)
        );

        List<TaskSnapshot> result = taskService.getAbandonedLifetimeTasks(tasks);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.isDeleted()));
        assertTrue(result.stream().noneMatch(t -> t.getStatus().equals("COMPLETED")));
    }

    private TaskSnapshot snap(LocalDateTime created, LocalDateTime completed) {
        TaskSnapshot t = new TaskSnapshot();
        t.setCreatedOn(created);
        t.setCompletedOn(completed);
        return t;
    }

    private TaskSnapshot snap(String status) {
        TaskSnapshot t = new TaskSnapshot();
        t.setStatus(status);
        return t;
    }

    private TaskSnapshot snap(String status, boolean deleted) {
        TaskSnapshot t = new TaskSnapshot();
        t.setStatus(status);
        t.setDeleted(deleted);
        return t;
    }
}
