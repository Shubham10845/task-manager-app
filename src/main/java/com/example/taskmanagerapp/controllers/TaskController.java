package com.example.taskmanagerapp.controllers;

import com.example.taskmanagerapp.dto.TaskRequestDTO;
import com.example.taskmanagerapp.dto.TaskResponseDTO;
import com.example.taskmanagerapp.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    @Autowired
    TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody TaskRequestDTO dto) {
        TaskResponseDTO responseDTO = taskService.saveTask(dto);
        return ResponseEntity.status(201).body(responseDTO);
    }
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTasksById(@PathVariable("id") String id) {
        TaskResponseDTO responseDTO = taskService.getTaskById(id);
        return ResponseEntity.ok(responseDTO);
    }
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable("id") String id, @RequestBody TaskRequestDTO dto) {
        TaskResponseDTO responseDTO = taskService.updateTask(id, dto);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") String id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/page/{page}/size/{size}")
    public ResponseEntity<?> getAllTasks(@PathVariable("page") int page, @PathVariable("size") int size) {
        return ResponseEntity.ok(taskService.getAllTasks(page, size));
    }
}
