package com.example.analytics_svc.service;

import com.example.analytics_svc.model.TaskAnalytics;
import com.example.analytics_svc.model.TaskSnapshot;
import com.example.analytics_svc.repository.TaskAnalyticsRepository;
import com.example.analytics_svc.repository.TaskSnapshotRepository;
import com.example.analytics_svc.web.dto.TaskAnalyticsRequest;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskSnapshotRepository snapshotRepository;
    private final TaskAnalyticsRepository taskAnalyticsRepository;
    private final EntityManager entityManager;

    public TaskService(TaskSnapshotRepository taskSnapshotRepository, TaskAnalyticsRepository analyticsRepository, EntityManager entityManager) {
        this.snapshotRepository = taskSnapshotRepository;
        this.taskAnalyticsRepository = analyticsRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public TaskAnalytics upsertTasks(List<TaskAnalyticsRequest> tasks, UUID userId) {

        if (tasks == null || tasks.isEmpty()) {
            return emptyAnalytics(userId);
        }

        snapshotRepository.deleteAllByUserId(userId);
        snapshotRepository.flush();
        entityManager.clear();

        for (TaskAnalyticsRequest t : tasks) {

            TaskSnapshot snap = TaskSnapshot.builder().taskId(t.getTaskId()).build();

            snap.setUserId(userId);
            snap.setStatus(t.getStatus());
            snap.setPriority(t.getPriority());
            snap.setCreatedOn(t.getCreatedOn());
            snap.setDueDate(t.getDueDate());
            snap.setCompletedOn(t.getCompletedOn());
            snap.setDeleted(t.isDeleted());

            snapshotRepository.save(snap);
        }

        TaskAnalytics analytics = getTasksAnalytics(snapshotRepository.findAllByUserIdAndDeletedFalse(userId), snapshotRepository.findAllByUserId(userId), userId);
        analytics.setUserId(userId);

        taskAnalyticsRepository.findByUserId(userId)
                .ifPresent(task -> analytics.setId(task.getId()));

        return taskAnalyticsRepository.save(analytics);
    }


    public TaskAnalytics getTasksAnalytics(List<TaskSnapshot> current, List<TaskSnapshot> lifetime, UUID userId) {

        return TaskAnalytics.builder()
                .userId(userId)
                .totalTasks(current.size())
                .completedTasks(getCompletedTasks(current).size())
                .inProgressTasks(getInProgressTasks(current))
                .todoTasks(getTodoTasks(current))
                .overdueTasks(getOverdueTasks(current))
                .completionRate(getCompletionRate(current))
                .avgCompletionTime(averageCompletionTime(current))
                .lowPriorityCount(getLowPriorityTasks(current))
                .mediumPriorityCount(getMediumPriorityTasks(current))
                .highPriorityCount(getHighPriorityTasks(current))
                .lifetimeTotalTasks(lifetime.size())
                .lifetimeCompletedTasks(getCompletedTasks(lifetime).size())
                .lifetimeAbandonedTasks(getAbandonedLifetimeTasks(lifetime).size())
                .lifetimeOverdueTasks(getLifetimeOverdueTasks(lifetime).size())
                .lifetimeAverageCompletionTime(averageCompletionTime(lifetime))
                .lifetimeCompletionRate((int) getCompletionRate(lifetime))
                .fastestCompletionTime(getFastestCompletionTime(lifetime))
                .build();

    }

    private long getFastestCompletionTime(List<TaskSnapshot> lifetime) {
        return lifetime.stream().filter(t -> t.getCreatedOn() != null && t.getCompletedOn() != null)
                .map(t -> Duration.between(t.getCreatedOn(), t.getCompletedOn())).sorted().findFirst()
                .map(Duration::toHours).orElse(0L);
    }


    private List<TaskSnapshot> getLifetimeOverdueTasks(List<TaskSnapshot> lifetime) {
        return lifetime.stream().filter(t -> t.getStatus().equals("OVERDUE") && t.isDeleted()).toList();
    }

    private List<TaskSnapshot> getAbandonedLifetimeTasks(List<TaskSnapshot> lifetime) {
        return lifetime.stream().filter(t -> !t.getStatus().equals("COMPLETED") && t.isDeleted()).toList();
    }

    public TaskAnalytics emptyAnalytics(UUID userId) {
        return TaskAnalytics.builder()
                .userId(userId)
                .totalTasks(0)
                .completedTasks(0)
                .inProgressTasks(0)
                .todoTasks(0)
                .overdueTasks(0)
                .completionRate(0)
                .avgCompletionTime(0)
                .lowPriorityCount(0)
                .mediumPriorityCount(0)
                .highPriorityCount(0)
                .lifetimeTotalTasks(0)
                .lifetimeCompletedTasks(0)
                .lifetimeAbandonedTasks(0)
                .lifetimeOverdueTasks(0)
                .lifetimeAverageCompletionTime(0)
                .lifetimeCompletionRate(0)
                .fastestCompletionTime(0)
                .build();
    }

    private int getLowPriorityTasks(List<TaskSnapshot> tasks) {
        return (int) tasks.stream().filter(t -> t.getPriority().equals("LOW")).count();
    }

    private int getMediumPriorityTasks(List<TaskSnapshot> tasks) {
        return (int) tasks.stream().filter(t -> t.getPriority().equals("MEDIUM")).count();
    }

    private int getHighPriorityTasks(List<TaskSnapshot> tasks) {
        return (int) tasks.stream().filter(t -> t.getPriority().equals("HIGH")).count();
    }

    private int getTodoTasks(List<TaskSnapshot> tasks) {
        return (int) tasks.stream().filter(t -> t.getStatus().equals("TODO")).count();
    }

    private List<TaskSnapshot> getCompletedTasks(List<TaskSnapshot> tasks) {
        return  tasks.stream().filter(t -> t.getStatus().equals("COMPLETED")).toList();
    }

    private int getInProgressTasks(List<TaskSnapshot> tasks) {
        return (int) tasks.stream().filter(t -> t.getStatus().equals("IN_PROGRESS")).count();
    }

    private int getOverdueTasks(List<TaskSnapshot> tasks) {
        return (int) tasks.stream().filter(t -> t.getStatus().equals("OVERDUE")).count();
    }

    private double getCompletionRate(List<TaskSnapshot> tasks) {
        int totalTasks = tasks.size();
        int completedTasks = getCompletedTasks(tasks).size();

        if (totalTasks == 0) {
            return 0;
        }

        return ((double) completedTasks / totalTasks) * 100.0;
    }

    private double averageCompletionTime(List<TaskSnapshot> tasks) {
        if (tasks.isEmpty()) {
            return 0;
        }

        List<Long> durationBetweenInDays = new ArrayList<>();

        for (TaskSnapshot task : tasks) {
            LocalDateTime completedOn = task.getCompletedOn();
            if (completedOn == null) {
                continue;
            }

            Duration duration = Duration.between(task.getCreatedOn(), completedOn);
            durationBetweenInDays.add(duration.toDays());
        }

        if (durationBetweenInDays.isEmpty()) {
            return 0;
        }

        double sum = durationBetweenInDays.stream()
                .mapToLong(Long::longValue)
                .sum();

        return sum / durationBetweenInDays.size();
    }

}
