package com.example.task_service.dto;

import com.example.task_service.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest (@NotNull TaskStatus status) {}
