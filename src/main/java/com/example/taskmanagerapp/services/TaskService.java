package com.example.taskmanagerapp.services;

import com.example.taskmanagerapp.exceptions.InvalidPageOrSizeException;
import com.example.taskmanagerapp.dto.TaskRequestDTO;
import com.example.taskmanagerapp.dto.PaginatedTaskResponseDTO;
import com.example.taskmanagerapp.dto.TaskResponseDTO;
import com.example.taskmanagerapp.exceptions.InvalidDateException;
import com.example.taskmanagerapp.exceptions.InvalidStatusException;
import com.example.taskmanagerapp.exceptions.InvalidTitleException;
import com.example.taskmanagerapp.exceptions.TaskNotFoundException;
import com.example.taskmanagerapp.mapper.TaskDtoMapper;
import com.example.taskmanagerapp.models.TaskModel;
import com.example.taskmanagerapp.repositories.TaskRepository;
import com.example.taskmanagerapp.util.IdGeneratorUtil;
import com.example.taskmanagerapp.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    TaskRepository taskRepository;

    public TaskResponseDTO saveTask(TaskRequestDTO dto) {
        if(dto.getTitle() == null || dto.getTitle().isEmpty()) {
            throw new InvalidTitleException("Title is required");
        }
        if(dto.getDueDate() == null || dto.getDueDate().isEmpty()) {
            throw new InvalidDateException("Due date is required");
        }
        // Convert DTO to TaskModel
        TaskModel task = TaskDtoMapper.toEntity(dto);
        // Validate if date is in future
        if (task.getDueDate().isBefore(LocalDate.now())) {
            throw new InvalidDateException("Invalid date, date should be of future");
        }

        task.setId(IdGeneratorUtil.generateId());
        taskRepository.saveTask(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus() != null ? task.getStatus().name() : null,
            task.getDueDate()
        );
        TaskModel savedTask = taskRepository.selectTask(task.getId());
        return  TaskDtoMapper.toResponseDto(savedTask);
    }

    public TaskResponseDTO getTaskById(String id) {
        TaskModel task = taskRepository.selectTask(id);
        if(task == null) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        return TaskDtoMapper.toResponseDto(task);
    }

    public TaskResponseDTO updateTask(String id, TaskRequestDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new InvalidTitleException("Title is required");
        }
        if (dto.getDueDate() == null || dto.getDueDate().isEmpty()) {
            throw new InvalidDateException("Due date is required");
        }
        if(dto.getStatus() == null || dto.getStatus().isEmpty()) {
            throw new InvalidStatusException("Status is required");
        }

        if( !TaskUtils.isValidTaskStatus(dto.getStatus())) {
            throw new InvalidStatusException("Valid statuses are: PENDING, IN_PROGRESS, DONE");
        }

        TaskModel updatedTask = TaskDtoMapper.toEntity(dto);

        // Validate if date is in future
        if (updatedTask.getDueDate().isBefore(LocalDate.now())) {
            throw new InvalidDateException("Invalid date, date should be of future");
        }

        boolean isUpdated = taskRepository.updateTask(
            id,
            updatedTask.getTitle(),
            updatedTask.getDescription(),
            updatedTask.getStatus() != null ? updatedTask.getStatus().name() : null,
            updatedTask.getDueDate()
        );

        if (!isUpdated) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }

        TaskModel savedTask = taskRepository.selectTask(id);
        return TaskDtoMapper.toResponseDto(savedTask);
    }

    public boolean deleteTaskById(String id) {
        boolean result =  taskRepository.deleteTask(id);
        if(!result) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        return result;
    }

    public PaginatedTaskResponseDTO getAllTasks(int page, int size) {
        if(page < 0 || size <= 0) {
            throw new InvalidPageOrSizeException("Page must be >= 0 and size must be > 0");
        }
        int offset = page * size;
        List<TaskModel> tasks = taskRepository.selectAllTasksPaginated(size, offset);
        List<TaskResponseDTO> taskDTOs = new ArrayList<>();
        for(TaskModel task : tasks){
            taskDTOs.add(TaskDtoMapper.toResponseDto(task));
        }
        long totalCount = taskRepository.countAllTasks();
        boolean hasMore = (offset + size) < totalCount;
        return new PaginatedTaskResponseDTO(taskDTOs, hasMore, totalCount, page, size);
    }

}
