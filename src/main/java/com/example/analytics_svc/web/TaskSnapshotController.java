package com.example.analytics_svc.web;

import com.example.analytics_svc.domain.AnalyticsNotFound;
import com.example.analytics_svc.model.TaskAnalytics;
import com.example.analytics_svc.repository.TaskAnalyticsRepository;
import com.example.analytics_svc.repository.TaskSnapshotRepository;
import com.example.analytics_svc.service.TaskService;
import com.example.analytics_svc.web.dto.TaskAnalyticsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskSnapshotController {

    private final TaskService taskService;
    private final TaskAnalyticsRepository taskAnalyticsRepository;
    private final TaskSnapshotRepository taskSnapshotRepository;

    public TaskSnapshotController(TaskService taskService, TaskAnalyticsRepository taskAnalyticsRepository, TaskSnapshotRepository taskSnapshotRepository) {
        this.taskService = taskService;
        this.taskAnalyticsRepository = taskAnalyticsRepository;
        this.taskSnapshotRepository = taskSnapshotRepository;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<TaskAnalytics> updateAnalytics(@RequestBody List<TaskAnalyticsRequest> tasks, @PathVariable UUID userId) {

        if (tasks == null || tasks.isEmpty()) {
            taskSnapshotRepository.deleteAllByUserId(userId);
            TaskAnalytics taskAnalytics = taskService.emptyAnalytics(userId);
            return ResponseEntity.ok(taskAnalytics);
        }
        TaskAnalytics taskAnalytics = taskService.upsertTasks(tasks, userId);

        return ResponseEntity.ok(taskAnalytics);
    }

    @GetMapping("/{userId}")
    public TaskAnalytics getTaskAnalytics(@PathVariable UUID userId) {
        try {
            return taskAnalyticsRepository.findByUserId(userId)
                    .orElseThrow(() -> new AnalyticsNotFound("No analytics for user: " + userId));
        } catch (AnalyticsNotFound e) {
            TaskAnalytics taskAnalytics = taskService.emptyAnalytics(userId);
            return taskAnalyticsRepository.save(taskAnalytics);
        }

    }

}
