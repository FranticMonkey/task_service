package com.example.task_service.controller;

import com.example.task_service.dto.TaskAssignRequest;
import com.example.task_service.dto.TaskCreateRequest;
import com.example.task_service.dto.TaskResponse;
import com.example.task_service.dto.TaskStatusUpdateRequest;
import com.example.task_service.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskService.getAllTasks(pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody TaskCreateRequest request) {
        return taskService.createTask(request);
    }

    @PostMapping("/{taskId}/assign")
    public TaskResponse assignExecutor(@PathVariable Long taskId, @Valid @RequestBody TaskAssignRequest request) {
        return taskService.assignExecutor(taskId, request.userId());
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse changeStatus(@PathVariable Long taskId, @Valid @RequestBody TaskStatusUpdateRequest request) {
        return taskService.changeStatus(taskId, request);
    }
}