package com.example.task_service.event;

public record TaskCreatedEvent(Long taskId, String name, String description) {
}
