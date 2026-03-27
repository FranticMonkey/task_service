package com.example.task_service.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskCreateRequest(@NotBlank String name, String description) {}
