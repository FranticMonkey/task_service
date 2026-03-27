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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "Alice", "alice@example.com");
        testTask = new Task(1L, "Test Task", "Description", TaskStatus.NEW, null);
    }

    @Test
    void getAllTasks_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(testTask));
        when(taskRepository.findAll(pageable)).thenReturn(taskPage);

        Page<TaskResponse> result = taskService.getAllTasks(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Test Task");
    }

    @Test
    void getTaskById_whenExists_shouldReturnTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        TaskResponse response = taskService.getTaskById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Test Task");
    }

    @Test
    void getTaskById_whenNotFound_shouldThrowException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 99");
    }

    @Test
    void createTask_shouldSaveAndSendEvent() {
        TaskCreateRequest request = new TaskCreateRequest("New Task", "New Desc");
        Task savedTask = new Task(2L, "New Task", "New Desc", TaskStatus.NEW, null);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        assertThat(response.name()).isEqualTo("New Task");
        ArgumentCaptor<TaskCreatedEvent> eventCaptor = ArgumentCaptor.forClass(TaskCreatedEvent.class);
        verify(kafkaProducerService).sendTaskEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().taskId()).isEqualTo(2L);
    }

    @Test
    void assignExecutor_shouldUpdateAndSendEvent() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.assignExecutor(1L, 1L);

        assertThat(response.executor()).isNotNull();
        assertThat(response.executor().name()).isEqualTo("Alice");
        ArgumentCaptor<TaskAssignedEvent> eventCaptor = ArgumentCaptor.forClass(TaskAssignedEvent.class);
        verify(kafkaProducerService).sendTaskEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(1L);
    }

    @Test
    void changeStatus_shouldUpdateStatus() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.IN_PROGRESS);
        TaskResponse response = taskService.changeStatus(1L, request);

        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }
}