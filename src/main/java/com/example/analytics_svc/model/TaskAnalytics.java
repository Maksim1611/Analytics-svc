package com.example.analytics_svc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private int totalTasks;

    @Column(nullable = false)
    private int completedTasks;

    @Column(nullable = false)
    private int inProgressTasks;

    @Column(nullable = false)
    private int todoTasks;

    @Column(nullable = false)
    private int overdueTasks;

    @Column(nullable = false)
    private double completionRate;

    @Column(nullable = false)
    private double avgCompletionTime;

    @Column(nullable = false)
    private int lowPriorityCount;

    @Column(nullable = false)
    private int mediumPriorityCount;

    @Column(nullable = false)
    private int highPriorityCount;

    @Column(nullable = false)
    private int lifetimeTotalTasks;

    @Column(nullable = false)
    private int lifetimeCompletedTasks;

    @Column(nullable = false)
    private int lifetimeAbandonedTasks;

    @Column(nullable = false)
    private int lifetimeOverdueTasks;

    @Column(nullable = false)
    private double lifetimeAverageCompletionTime;

    @Column(nullable = false)
    private long fastestCompletionTime;

    @Column(nullable = false)
    private int lifetimeCompletionRate;
}
