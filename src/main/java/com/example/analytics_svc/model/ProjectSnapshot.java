package com.example.analytics_svc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private UUID projectId;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private int completionPercentage;

    private LocalDateTime completedOn;

    @Column(nullable = false)
    private boolean deleted;

}
