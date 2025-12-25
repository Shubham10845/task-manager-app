package com.example.taskmanagerapp.integrationtests;

import com.example.taskmanagerapp.dto.TaskRequestDTO;
import com.example.taskmanagerapp.dto.TaskResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void taskFullLifecycleIntegrationTest() throws Exception {
//       STEP 1: CREATE Task
        TaskRequestDTO createRequest = new TaskRequestDTO();
        createRequest.setTitle("Integration Test Task");
        createRequest.setDescription("Testing full workflow");
        createRequest.setStatus("PENDING");
        createRequest.setDueDate("2026-06-30"); // Future date

        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Testing full workflow"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        // Extract task ID from response
        String responseContent = createResult.getResponse().getContentAsString();
        TaskResponseDTO createdTask = objectMapper.readValue(responseContent, TaskResponseDTO.class);
        String taskId = createdTask.getId();

//        STEP 2: GET Task by ID - Verify Creation
        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Testing full workflow"))
                .andExpect(jsonPath("$.status").value("PENDING"));

//      STEP 3: UPDATE Task
        TaskRequestDTO updateRequest = new TaskRequestDTO();
        updateRequest.setTitle("Updated Integration Task");
        updateRequest.setDescription("Updated description for testing");
        updateRequest.setStatus("IN_PROGRESS");
        updateRequest.setDueDate("2026-07-15"); // Future date

        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Task"))
                .andExpect(jsonPath("$.description").value("Updated description for testing"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

//      STEP 4: GET Task Again - Verify Update
        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Updated Integration Task"))
                .andExpect(jsonPath("$.description").value("Updated description for testing"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

//      STEP 5: CREATE Second Task for Pagination Testing
        TaskRequestDTO secondTaskRequest = new TaskRequestDTO();
        secondTaskRequest.setTitle("Second Task");
        secondTaskRequest.setDescription("Testing pagination");
        secondTaskRequest.setStatus("DONE");
        secondTaskRequest.setDueDate("2026-08-10");

        MvcResult secondCreateResult = mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondTaskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Second Task"))
                .andReturn();

        String secondResponseContent = secondCreateResult.getResponse().getContentAsString();
        TaskResponseDTO secondCreatedTask = objectMapper.readValue(secondResponseContent, TaskResponseDTO.class);
        String secondTaskId = secondCreatedTask.getId();

//        STEP 6: GET All Tasks - Should include both tasks
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 0, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks").isNotEmpty())
                .andExpect(jsonPath("$.tasks.length()").value(2))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.hasMore").value(false));

//        STEP 7: Test Pagination - Page size 1
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 0, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks.length()").value(1))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.hasMore").value(true)); // Should have more pages

//         STEP 8: DELETE First Task
        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

//        STEP 9: GET Deleted Task - Should return 404
        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNotFound());

//        STEP 10: GET All Tasks - Should have only one task now
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 0, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks.length()").value(1))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.hasMore").value(false));

//        Verify remaining task is the second one
        mockMvc.perform(get("/api/v1/tasks/{id}", secondTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Second Task"))
                .andExpect(jsonPath("$.status").value("DONE"));

//        STEP 12: DELETE Second Task
        mockMvc.perform(delete("/api/v1/tasks/{id}", secondTaskId))
                .andExpect(status().isNoContent());

//      STEP 13: GET All Tasks - Should be empty now
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 0, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks").isEmpty())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void taskValidationIntegrationTest() throws Exception {
//        Test Invalid Title in create task
        TaskRequestDTO invalidTitleRequest = new TaskRequestDTO();
        invalidTitleRequest.setTitle(""); // Empty title
        invalidTitleRequest.setDescription("Test description");
        invalidTitleRequest.setStatus("PENDING");
        invalidTitleRequest.setDueDate("2026-06-30");

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTitleRequest)))
                .andExpect(status().isBadRequest());

//      Test Invalid Date (Past Date) in create task
        TaskRequestDTO pastDateRequest = new TaskRequestDTO();
        pastDateRequest.setTitle("Test Task");
        pastDateRequest.setDescription("Test description");
        pastDateRequest.setStatus("PENDING");
        pastDateRequest.setDueDate("2020-01-01"); // Past date

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pastDateRequest)))
                .andExpect(status().isBadRequest());

        // ===================== Test Invalid Status in Update =====================
        // First create a valid task
        TaskRequestDTO validRequest = new TaskRequestDTO();
        validRequest.setTitle("Valid Task");
        validRequest.setDescription("Valid description");
        validRequest.setStatus("PENDING");
        validRequest.setDueDate("2026-06-30");

        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        TaskResponseDTO createdTask = objectMapper.readValue(responseContent, TaskResponseDTO.class);
        String taskId = createdTask.getId();

//        Try to update with invalid status
        TaskRequestDTO invalidStatusUpdate = new TaskRequestDTO();
        invalidStatusUpdate.setTitle("Updated Task");
        invalidStatusUpdate.setDescription("Updated description");
        invalidStatusUpdate.setStatus("INVALID_STATUS");
        invalidStatusUpdate.setDueDate("2026-07-15");

        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidStatusUpdate)))
                .andExpect(status().isBadRequest());

//      Clean up by deleting the created task
        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNoContent());
    }

    @Test
    void taskNotFoundIntegrationTest() throws Exception {
//        Test GET non-existent task
        mockMvc.perform(get("/api/v1/tasks/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());

//      Test UPDATE non-existent task
        TaskRequestDTO updateRequest = new TaskRequestDTO();
        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus("IN_PROGRESS");
        updateRequest.setDueDate("2026-06-30");

        mockMvc.perform(put("/api/v1/tasks/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

//      Test DELETE non-existent task
        mockMvc.perform(delete("/api/v1/tasks/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void paginationIntegrationTest() throws Exception {
//      Create multiple tasks for pagination testing
        for (int i = 1; i <= 7; i++) {
            TaskRequestDTO taskRequest = new TaskRequestDTO();
            taskRequest.setTitle("Task " + i);
            taskRequest.setDescription("Description for task " + i);
            taskRequest.setStatus("PENDING");
            taskRequest.setDueDate("2026-0" + (i % 9 + 1) + "-15");

            mockMvc.perform(post("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(taskRequest)))
                    .andExpect(status().isCreated());
        }

//     Test First Page
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 0, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.length()").value(3))
                .andExpect(jsonPath("$.total").value(7))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.hasMore").value(true));

//      Test Second Page
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 1, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.length()").value(3))
                .andExpect(jsonPath("$.total").value(7))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.hasMore").value(true));

//        Test Last Page
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 2, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.length()").value(1))
                .andExpect(jsonPath("$.total").value(7))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.hasMore").value(false));

//      Test  Dates are ordered ascending
        MvcResult result = mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 0, 7))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode tasks = objectMapper.readTree(content).get("tasks");

        for (int i = 0; i < tasks.size() - 1; i++) {
            LocalDate current = LocalDate.parse(tasks.get(i).get("dueDate").asText());
            LocalDate next = LocalDate.parse(tasks.get(i + 1).get("dueDate").asText());
            Assertions.assertFalse(current.isAfter(next), "Tasks are not ordered by due date ascending");
        }

//        Test Invalid Page Parameters
        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", -1, 5))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/tasks/page/{page}/size/{size}", 0, 0))
                .andExpect(status().isBadRequest());
    }
}
