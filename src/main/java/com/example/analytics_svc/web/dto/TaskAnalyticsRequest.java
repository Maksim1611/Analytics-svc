package com.example.analytics_svc.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAnalyticsRequest {

    private UUID taskId;

    private UUID userId;

    private String status;

    private String priority;

    private LocalDateTime createdOn;

    private LocalDateTime dueDate;

    private LocalDateTime completedOn;

    private boolean deleted;

}
