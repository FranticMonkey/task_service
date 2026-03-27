package com.example.task_service.event;

public record TaskAssignedEvent(Long taskId, Long userId, String userName) {
}
