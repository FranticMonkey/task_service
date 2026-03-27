package com.example.task_service.controller;

import com.example.task_service.dto.TaskAssignRequest;
import com.example.task_service.dto.TaskCreateRequest;
import com.example.task_service.dto.TaskStatusUpdateRequest;
import com.example.task_service.entity.TaskStatus;
import com.example.task_service.repository.TaskRepository;
import com.example.task_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"task-events"})
@DirtiesContext
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<TaskCreateRequest> taskCreateRequestTester;

    @Autowired
    private JacksonTester<TaskAssignRequest> taskAssignRequestTester;

    @Autowired
    private JacksonTester<TaskStatusUpdateRequest> taskStatusUpdateRequestTester;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(new com.example.task_service.entity.User(null, "Bob", "bob@example.com"));
    }

    @Test
    void createTask_shouldReturnCreated() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest("Integration Task", "Test Desc");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskCreateRequestTester.write(request).getJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Task"));
    }

    @Test
    void getTaskById_shouldReturnTask() throws Exception {
        var task = new com.example.task_service.entity.Task(null, "Get Task", "Desc", TaskStatus.NEW, null);
        var saved = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Get Task"));
    }

    @Test
    void assignExecutor_shouldUpdateTask() throws Exception {
        var task = new com.example.task_service.entity.Task(null, "Assign Task", "Desc", TaskStatus.NEW, null);
        var savedTask = taskRepository.save(task);
        var user = userRepository.findAll().getFirst();

        TaskAssignRequest assignRequest = new TaskAssignRequest(user.getId());

        mockMvc.perform(post("/api/tasks/{taskId}/assign", savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskAssignRequestTester.write(assignRequest).getJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executor.name").value("Bob"));
    }

    @Test
    void changeStatus_shouldUpdateStatus() throws Exception {
        var task = new com.example.task_service.entity.Task(null, "Change Status", "Desc", TaskStatus.NEW, null);
        var savedTask = taskRepository.save(task);

        TaskStatusUpdateRequest statusRequest = new TaskStatusUpdateRequest(TaskStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tasks/{taskId}/status", savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskStatusUpdateRequestTester.write(statusRequest).getJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getNonExistentTask_shouldReturn404WithProblemDetail() throws Exception {
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Task not found with id: 999"));
    }

    @Test
    void createTaskWithInvalidName_shouldReturn400WithValidationErrors() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest("", "Invalid");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskCreateRequestTester.write(request).getJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.errors.name").exists());
    }
}