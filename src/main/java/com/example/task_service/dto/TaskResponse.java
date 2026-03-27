package com.example.task_service.dto;

import com.example.task_service.entity.TaskStatus;

public record TaskResponse (
    Long id,
    String name,
    String description,
    TaskStatus status,
    UserDto executor
) {}
