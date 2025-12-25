package com.example.taskmanagerapp.services;

import com.example.taskmanagerapp.dto.PaginatedTaskResponseDTO;
import com.example.taskmanagerapp.dto.TaskRequestDTO;
import com.example.taskmanagerapp.dto.TaskResponseDTO;
import com.example.taskmanagerapp.enums.TaskStatus;
import com.example.taskmanagerapp.exceptions.*;
import com.example.taskmanagerapp.models.TaskModel;
import com.example.taskmanagerapp.repositories.TaskRepository;
import com.example.taskmanagerapp.util.IdGeneratorUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private TaskRequestDTO taskRequestDTO;
    private TaskModel taskModel;

    @BeforeEach
    void setUp() {
        // Setup test data with future dates
        taskRequestDTO = new TaskRequestDTO();
        taskRequestDTO.setTitle("Test Task");
        taskRequestDTO.setDescription("Test Description");
        taskRequestDTO.setStatus("PENDING");
        taskRequestDTO.setDueDate("2026-06-30");

        taskModel = new TaskModel();
        taskModel.setId("test-id-123");
        taskModel.setTitle("Test Task");
        taskModel.setDescription("Test Description");
        taskModel.setStatus(TaskStatus.PENDING);
        taskModel.setDueDate(LocalDate.of(2026, 6, 30));
    }

    //  SAVE TASK TESTS
    @Test
    void saveTaskShouldReturnTaskResponseDTOWhenValidTask() {
        try (MockedStatic<IdGeneratorUtil> mockedStatic = mockStatic(IdGeneratorUtil.class)) {
            mockedStatic.when(IdGeneratorUtil::generateId).thenReturn("test-id-123");
            when(taskRepository.selectTask("test-id-123")).thenReturn(taskModel);

            TaskResponseDTO result = taskService.saveTask(taskRequestDTO);

            assertNotNull(result);
            assertEquals("test-id-123", result.getId());
            assertEquals("Test Task", result.getTitle());
            assertEquals("Test Description", result.getDescription());
            assertEquals("PENDING", result.getStatus());
            assertEquals(LocalDate.of(2026, 6, 30), result.getDueDate());

            verify(taskRepository).saveTask(eq("test-id-123"), eq("Test Task"),
                    eq("Test Description"), eq("PENDING"), eq(LocalDate.of(2026, 6, 30)));
        }
    }

    @Test
    void saveTaskShouldThrowInvalidTitleExceptionWhenTitleIsNull() {
        taskRequestDTO.setTitle(null);

        InvalidTitleException exception = assertThrows(InvalidTitleException.class,
                () -> taskService.saveTask(taskRequestDTO));

        assertEquals("Title is required", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void saveTaskShouldThrowInvalidTitleExceptionWhenTitleIsEmpty() {
        taskRequestDTO.setTitle("");

        InvalidTitleException exception = assertThrows(InvalidTitleException.class,
                () -> taskService.saveTask(taskRequestDTO));

        assertEquals("Title is required", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void saveTaskShouldThrowInvalidDateExceptionWhenDueDateIsNull() {
        taskRequestDTO.setDueDate(null);

        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> taskService.saveTask(taskRequestDTO));

        assertEquals("Due date is required", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void saveTaskShouldThrowInvalidDateExceptionWhenDueDateIsEmpty() {
        taskRequestDTO.setDueDate("");

        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> taskService.saveTask(taskRequestDTO));

        assertEquals("Due date is required", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void saveTaskShouldThrowInvalidDateExceptionWhenDueDateIsInPast() {
        taskRequestDTO.setDueDate("2020-01-01");

        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> taskService.saveTask(taskRequestDTO));

        assertEquals("Invalid date, date should be of future", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void saveTaskShouldAcceptFutureDateWhenDueDateIsTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        taskRequestDTO.setDueDate(tomorrow.toString());

        TaskModel tomorrowTaskModel = new TaskModel();
        tomorrowTaskModel.setId("tomorrow-id");
        tomorrowTaskModel.setTitle("Test Task");
        tomorrowTaskModel.setDescription("Test Description");
        tomorrowTaskModel.setStatus(TaskStatus.PENDING);
        tomorrowTaskModel.setDueDate(tomorrow);

        try (MockedStatic<IdGeneratorUtil> mockedStatic = mockStatic(IdGeneratorUtil.class)) {
            mockedStatic.when(IdGeneratorUtil::generateId).thenReturn("tomorrow-id");
            when(taskRepository.selectTask("tomorrow-id")).thenReturn(tomorrowTaskModel);

            TaskResponseDTO result = taskService.saveTask(taskRequestDTO);

            assertNotNull(result);
            assertEquals("tomorrow-id", result.getId());
            assertEquals(tomorrow, result.getDueDate());
        }
    }

//  GET TASK BY ID TESTS
    @Test
    void getTaskByIdShouldReturnTaskResponseDTOWhenTaskExists() {
        String taskId = "test-id-123";
        when(taskRepository.selectTask(taskId)).thenReturn(taskModel);

        TaskResponseDTO result = taskService.getTaskById(taskId);

        assertNotNull(result);
        assertEquals("test-id-123", result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).selectTask(taskId);
    }

    @Test
    void getTaskByIdShouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
        String taskId = "non-existent-id";
        when(taskRepository.selectTask(taskId)).thenReturn(null);

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.getTaskById(taskId));

        assertEquals("Task not found with id: non-existent-id", exception.getMessage());
        verify(taskRepository).selectTask(taskId);
    }

//    UPDATE TASK TESTS
    @Test
    void updateTaskShouldReturnUpdatedTaskWhenValidRequest() {
        String taskId = "test-id-123";
        taskRequestDTO.setTitle("Updated Task");
        taskRequestDTO.setStatus("IN_PROGRESS");

        TaskModel updatedTaskModel = new TaskModel();
        updatedTaskModel.setId(taskId);
        updatedTaskModel.setTitle("Updated Task");
        updatedTaskModel.setDescription("Test Description");
        updatedTaskModel.setStatus(TaskStatus.IN_PROGRESS);
        updatedTaskModel.setDueDate(LocalDate.of(2026, 6, 30));

        when(taskRepository.updateTask(eq(taskId), eq("Updated Task"), eq("Test Description"),
                eq("IN_PROGRESS"), eq(LocalDate.of(2026, 6, 30)))).thenReturn(true);
        when(taskRepository.selectTask(taskId)).thenReturn(updatedTaskModel);

        TaskResponseDTO result = taskService.updateTask(taskId, taskRequestDTO);

        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
        assertEquals("IN_PROGRESS", result.getStatus());
        verify(taskRepository).updateTask(eq(taskId), eq("Updated Task"), eq("Test Description"),
                eq("IN_PROGRESS"), eq(LocalDate.of(2026, 6, 30)));
    }

    @Test
    void updateTaskShouldThrowInvalidTitleExceptionWhenTitleIsNull() {
        String taskId = "test-id-123";
        taskRequestDTO.setTitle(null);

        InvalidTitleException exception = assertThrows(InvalidTitleException.class,
                () -> taskService.updateTask(taskId, taskRequestDTO));

        assertEquals("Title is required", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateTaskShouldThrowInvalidTitleExceptionWhenTitleIsEmptyAfterTrim() {
        String taskId = "test-id-123";
        taskRequestDTO.setTitle("   ");

        InvalidTitleException exception = assertThrows(InvalidTitleException.class,
                () -> taskService.updateTask(taskId, taskRequestDTO));

        assertEquals("Title is required", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateTaskShouldThrowInvalidDateExceptionWhenDueDateIsNull() {
        String taskId = "test-id-123";
        taskRequestDTO.setDueDate(null);

        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> taskService.updateTask(taskId, taskRequestDTO));

        assertEquals("Due date is required", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateTaskShouldThrowInvalidStatusExceptionWhenStatusIsNull() {
        String taskId = "test-id-123";
        taskRequestDTO.setStatus(null);

        InvalidStatusException exception = assertThrows(InvalidStatusException.class,
                () -> taskService.updateTask(taskId, taskRequestDTO));

        assertEquals("Status is required", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateTaskShouldThrowInvalidStatusExceptionWhenStatusIsEmpty() {
        String taskId = "test-id-123";
        taskRequestDTO.setStatus("");

        InvalidStatusException exception = assertThrows(InvalidStatusException.class,
                () -> taskService.updateTask(taskId, taskRequestDTO));

        assertEquals("Status is required", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateTaskShouldThrowInvalidStatusExceptionWhenStatusIsInvalid() {
        String taskId = "test-id-123";
        taskRequestDTO.setStatus("INVALID_STATUS");

        InvalidStatusException exception = assertThrows(InvalidStatusException.class,
                () -> taskService.updateTask(taskId, taskRequestDTO));

        assertEquals("Valid statuses are: PENDING, IN_PROGRESS, DONE", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateTaskShouldThrowInvalidDateExceptionWhenDueDateIsInPast() {
        String taskId = "test-id-123";
        taskRequestDTO.setDueDate("2020-01-01");

        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> taskService.updateTask(taskId, taskRequestDTO));

        assertEquals("Invalid date, date should be of future", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateTaskShouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
        String taskId = "non-existent-id";
        when(taskRepository.updateTask(any(), any(), any(), any(), any())).thenReturn(false);

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask(taskId, taskRequestDTO));

        assertEquals("Task not found with id: non-existent-id", exception.getMessage());
    }

//    DELETE TASK TESTS
    @Test
    void deleteTaskByIdShouldReturnTrueWhenTaskExists() {
        String taskId = "test-id-123";
        when(taskRepository.deleteTask(taskId)).thenReturn(true);

        boolean result = taskService.deleteTaskById(taskId);

        assertTrue(result);
        verify(taskRepository).deleteTask(taskId);
    }

    @Test
    void deleteTaskByIdShouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
        String taskId = "non-existent-id";
        when(taskRepository.deleteTask(taskId)).thenReturn(false);

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.deleteTaskById(taskId));

        assertEquals("Task not found with id: non-existent-id", exception.getMessage());
        verify(taskRepository).deleteTask(taskId);
    }

//  GET ALL TASKS TESTS
    @Test
    void getAllTasksShouldReturnPaginatedResponseWhenValidPageAndSize() {
        int page = 0;
        int size = 5;
        List<TaskModel> tasks = Arrays.asList(taskModel);
        long totalCount = 10L;

        when(taskRepository.selectAllTasksPaginated(size, 0)).thenReturn(tasks);
        when(taskRepository.countAllTasks()).thenReturn(totalCount);

        PaginatedTaskResponseDTO result = taskService.getAllTasks(page, size);

        assertNotNull(result);
        assertEquals(1, result.getTasks().size());
        assertTrue(result.isHasMore()); // (0 + 5) < 10 = true
        assertEquals(totalCount, result.getTotal());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());

        verify(taskRepository).selectAllTasksPaginated(size, 0);
        verify(taskRepository).countAllTasks();
    }

    @Test
    void getAllTasksShouldThrowInvalidPageOrSizeExceptionWhenPageIsNegative() {
        int page = -1;
        int size = 5;

        InvalidPageOrSizeException exception = assertThrows(InvalidPageOrSizeException.class,
                () -> taskService.getAllTasks(page, size));

        assertEquals("Page must be >= 0 and size must be > 0", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void getAllTasksShouldThrowInvalidPageOrSizeExceptionWhenSizeIsZero() {
        int page = 0;
        int size = 0;

        InvalidPageOrSizeException exception = assertThrows(InvalidPageOrSizeException.class,
                () -> taskService.getAllTasks(page, size));

        assertEquals("Page must be >= 0 and size must be > 0", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void getAllTasksShouldThrowInvalidPageOrSizeExceptionWhenSizeIsNegative() {
        int page = 0;
        int size = -1;

        InvalidPageOrSizeException exception = assertThrows(InvalidPageOrSizeException.class,
                () -> taskService.getAllTasks(page, size));

        assertEquals("Page must be >= 0 and size must be > 0", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void getAllTasksShouldReturnHasMoreFalseWhenNoMoreRecords() {
        int page = 1;
        int size = 5;
        List<TaskModel> tasks = Arrays.asList(taskModel);
        long totalCount = 5L;

        when(taskRepository.selectAllTasksPaginated(size, 5)).thenReturn(tasks);
        when(taskRepository.countAllTasks()).thenReturn(totalCount);

        PaginatedTaskResponseDTO result = taskService.getAllTasks(page, size);

        assertNotNull(result);
        assertFalse(result.isHasMore());
        verify(taskRepository).selectAllTasksPaginated(size, 5);
    }

    @Test
    void getAllTasksShouldReturnEmptyListWhenNoTasks() {
        int page = 0;
        int size = 5;
        List<TaskModel> emptyTasks = Arrays.asList();
        long totalCount = 0L;

        when(taskRepository.selectAllTasksPaginated(size, 0)).thenReturn(emptyTasks);
        when(taskRepository.countAllTasks()).thenReturn(totalCount);

        PaginatedTaskResponseDTO result = taskService.getAllTasks(page, size);

        assertNotNull(result);
        assertEquals(0, result.getTasks().size());
        assertFalse(result.isHasMore());
        assertEquals(0L, result.getTotal());
    }

    @Test
    void getAllTasksShouldCalculateOffsetCorrectlyForDifferentPages() {
        int page = 3;
        int size = 10;
        int expectedOffset = 30;
        List<TaskModel> tasks = Arrays.asList(taskModel);
        long totalCount = 100L;

        when(taskRepository.selectAllTasksPaginated(size, expectedOffset)).thenReturn(tasks);
        when(taskRepository.countAllTasks()).thenReturn(totalCount);

        taskService.getAllTasks(page, size);

        verify(taskRepository).selectAllTasksPaginated(size, expectedOffset);
    }

    @Test
    void getAllTasksShouldHandleMultipleTasks() {
        int page = 0;
        int size = 5;

        TaskModel task2 = new TaskModel();
        task2.setId("task-2");
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.setDueDate(LocalDate.of(2026, 7, 15));

        List<TaskModel> tasks = Arrays.asList(taskModel, task2);
        long totalCount = 15L;

        when(taskRepository.selectAllTasksPaginated(size, 0)).thenReturn(tasks);
        when(taskRepository.countAllTasks()).thenReturn(totalCount);

        PaginatedTaskResponseDTO result = taskService.getAllTasks(page, size);

        assertNotNull(result);
        assertEquals(2, result.getTasks().size());
        assertTrue(result.isHasMore());
        assertEquals(15L, result.getTotal());

        TaskResponseDTO firstTask = result.getTasks().get(0);
        assertEquals("test-id-123", firstTask.getId());
        assertEquals("Test Task", firstTask.getTitle());

        TaskResponseDTO secondTask = result.getTasks().get(1);
        assertEquals("task-2", secondTask.getId());
        assertEquals("Task 2", secondTask.getTitle());
        assertEquals("IN_PROGRESS", secondTask.getStatus());
    }
}
