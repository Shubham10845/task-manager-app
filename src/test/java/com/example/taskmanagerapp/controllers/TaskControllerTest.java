package com.example.taskmanagerapp.controllers;

import com.example.taskmanagerapp.dto.PaginatedTaskResponseDTO;
import com.example.taskmanagerapp.dto.TaskRequestDTO;
import com.example.taskmanagerapp.dto.TaskResponseDTO;
import com.example.taskmanagerapp.exceptions.InvalidPageOrSizeException;
import com.example.taskmanagerapp.exceptions.TaskNotFoundException;
import com.example.taskmanagerapp.services.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Arrays;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskRequestDTO taskRequestDTO;
    private TaskResponseDTO taskResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        taskRequestDTO = new TaskRequestDTO();
        taskRequestDTO.setTitle("Test Task");
        taskRequestDTO.setDescription("Test Description");
        taskRequestDTO.setStatus("PENDING");
        taskRequestDTO.setDueDate("2025-12-31");

        taskResponseDTO = new TaskResponseDTO();
        taskResponseDTO.setId("test-id-123");
        taskResponseDTO.setTitle("Test Task");
        taskResponseDTO.setDescription("Test Description");
        taskResponseDTO.setStatus("PENDING");
        taskResponseDTO.setDueDate(LocalDate.of(2025, 12, 31));
    }

    @Test
    void createTaskShouldReturnCreatedTaskWhenValidRequest() throws Exception {
        // Given
        when(taskService.saveTask(any(TaskRequestDTO.class))).thenReturn(taskResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("test-id-123"))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getTaskByIdShouldReturnTaskWhenTaskExists() throws Exception {
        // Given
        String taskId = "test-id-123";
        when(taskService.getTaskById(taskId)).thenReturn(taskResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id-123"))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void getTaskByIdShouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        // Given
        String taskId = "non-existent-id";
        when(taskService.getTaskById(taskId)).thenThrow(new TaskNotFoundException("Task not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskShouldReturnUpdatedTaskWhenValidRequest() throws Exception {
        // Given
        String taskId = "test-id-123";
        taskRequestDTO.setTitle("Updated Task");
        taskResponseDTO.setTitle("Updated Task");

        when(taskService.updateTask(eq(taskId), any(TaskRequestDTO.class))).thenReturn(taskResponseDTO);

        // When & Then
        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    void updateTaskShouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        // Given
        String taskId = "non-existent-id";
        when(taskService.updateTask(eq(taskId), any(TaskRequestDTO.class)))
                .thenThrow(new TaskNotFoundException("Task not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTaskShouldReturnNoContentWhenTaskExists() throws Exception {
        // Given
        String taskId = "test-id-123";
        when(taskService.deleteTaskById(taskId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTaskShouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        // Given
        String taskId = "non-existent-id";
        doThrow(new TaskNotFoundException("Task not found")).when(taskService).deleteTaskById(taskId);

        // When & Then
        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTasksShouldReturnPaginatedTasksWhenValidPageAndSize() throws Exception {
        // Given
        PaginatedTaskResponseDTO paginatedResponse = new PaginatedTaskResponseDTO(
                Arrays.asList(taskResponseDTO), false, 1L, 0, 5);

        when(taskService.getAllTasks(0, 5)).thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 0, 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks").isNotEmpty())
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void getAllTasksShouldReturnBadRequestWhenInvalidPageOrSize() throws Exception {
        // Given
        when(taskService.getAllTasks(-1, 0))
                .thenThrow(new InvalidPageOrSizeException("Page must be >= 0 and size must be > 0"));

        // When & Then
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", -1, 0))
                .andExpect(status().isBadRequest());
    }
}
