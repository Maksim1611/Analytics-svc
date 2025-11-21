package com.example.analytics_svc.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectAnalyticsRequest {

    private UUID userId;

    private UUID projectId;

    private LocalDateTime createdOn;

    private String status;

    private LocalDateTime dueDate;

    private int completionPercentage;

    private boolean deleted;

}
