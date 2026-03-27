package com.example.task_service.dto;


import jakarta.validation.constraints.NotNull;

public record TaskAssignRequest (@NotNull Long userId) {}
