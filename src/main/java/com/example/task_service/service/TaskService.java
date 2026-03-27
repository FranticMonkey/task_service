package com.example.task_service.service;

import com.example.task_service.dto.*;
import com.example.task_service.entity.Task;
import com.example.task_service.entity.TaskStatus;
import com.example.task_service.entity.User;
import com.example.task_service.event.TaskAssignedEvent;
import com.example.task_service.event.TaskCreatedEvent;
import com.example.task_service.exception.ResourceNotFoundException;
import com.example.task_service.repository.TaskRepository;
import com.example.task_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        Task task = new Task();
        task.setName(request.name());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.NEW);
        Task saved = taskRepository.save(task);

        kafkaProducerService.sendTaskEvent(new TaskCreatedEvent(saved.getId(), saved.getName(), saved.getDescription()));
        return mapToResponse(saved);
    }

    @Transactional
    public TaskResponse assignExecutor(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        task.setExecutor(user);
        Task updated = taskRepository.save(task);

        kafkaProducerService.sendTaskEvent(new TaskAssignedEvent(taskId, userId, user.getName()));
        return mapToResponse(updated);
    }

    @Transactional
    public TaskResponse changeStatus(Long taskId, TaskStatusUpdateRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        TaskStatus newStatus = request.status();

        task.setStatus(newStatus);
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    private TaskResponse mapToResponse(Task task) {
        UserDto executor = task.getExecutor() != null
                ? new UserDto(task.getExecutor().getId(), task.getExecutor().getName(), task.getExecutor().getEmail())
                : null;
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStatus(),
                executor
        );
    }
}