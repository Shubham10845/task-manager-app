package com.example.taskmanagerapp.mapper;

import com.example.taskmanagerapp.enums.TaskStatus;
import com.example.taskmanagerapp.dto.TaskRequestDTO;
import com.example.taskmanagerapp.dto.TaskResponseDTO;
import com.example.taskmanagerapp.models.TaskModel;

import java.time.LocalDate;

public class TaskDtoMapper {
    public static TaskModel toEntity(TaskRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        TaskModel task = new TaskModel();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        String status = dto.getStatus();
        if (status != null) {
            task.setStatus(Enum.valueOf(TaskStatus.class, status.toUpperCase()));
        } else {
            task.setStatus(null);
        }
        task.setDueDate(dto.getDueDate() != null ? LocalDate.parse(dto.getDueDate()) : null);
        return task;
    }

    public static TaskResponseDTO toResponseDto(TaskModel task) {
        if (task == null) {
            return null;
        }

        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        TaskStatus status = task.getStatus();
        if (status != null) {
            dto.setStatus(status.name());
        } else {
            dto.setStatus(null);
        }
        dto.setDueDate(task.getDueDate());

        return dto;
    }

}
