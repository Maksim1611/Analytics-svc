package com.example.analytics_svc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private int totalProjects;

    @Column(nullable = false)
    private int activeProjects;

    @Column(nullable = false)
    private int completedProjects;

    @Column(nullable = false)
    private int averageProgress;

    @Column(nullable = false)
    private int overdueProjects;

    @Column(nullable = false)
    private int totalProjectsLifetime;

    @Column(nullable = false)
    private int completedProjectsLifetime;

    @Column(nullable = false)
    private int abandonedProjectsLifetime;

    @Column(nullable = false)
    private long averageProjectDurationLifetime;

    @Column(nullable = false)
    private double projectCompletionRateLifetime;

}
