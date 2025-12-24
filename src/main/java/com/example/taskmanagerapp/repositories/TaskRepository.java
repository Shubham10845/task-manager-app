package com.example.taskmanagerapp.repositories;

import com.example.taskmanagerapp.enums.TaskStatus;
import com.example.taskmanagerapp.models.TaskModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class TaskRepository {
    String SAVE_TASK = "INSERT INTO task (id, title, description, status, due_date) VALUES (?, ?, ?, ?, ?)";
    String SELECT_TASK_BY_ID = "SELECT id, title, description, status, due_date FROM task WHERE id = ? and is_deleted = false";
    String UPDATE_TASK = "UPDATE task SET title = ?, description = ?, status = ?, due_date = ? WHERE id = ? AND is_deleted = false";
    String SOFT_DELETE_TASK = "UPDATE task SET is_deleted = true WHERE id = ?";

    String SELECT_ALL_TASKS_PAGINATED = "SELECT id, title, description, status, due_date FROM task WHERE is_deleted = false ORDER BY due_date ASC LIMIT ? OFFSET ?";
    private static final String COUNT_TASKS = "SELECT COUNT(*) FROM task WHERE is_deleted = false";

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void saveTask(String id, String title, String description, String status, LocalDate dueDate) {
        jdbcTemplate.update(SAVE_TASK, id, title, description, status, dueDate);
    }

    public TaskModel selectTask(String id) {
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(SELECT_TASK_BY_ID, id);
            TaskModel task = new TaskModel();
            task.setId((String) result.get("id"));
            task.setTitle((String) result.get("title"));
            task.setDescription((String) result.get("description"));
            task.setStatus(TaskStatus.valueOf((String) result.get("status")));
            // Handle date conversion properly - remove duplicate calls
            Object dueDateObj = result.get("due_date");
            if (dueDateObj != null) {
                if (dueDateObj instanceof java.sql.Date) {
                    task.setDueDate(((java.sql.Date) dueDateObj).toLocalDate());
                } else if (dueDateObj instanceof LocalDate) {
                    task.setDueDate((LocalDate) dueDateObj);
                }
            }
            return task;
        } catch (Exception e) {
            return null; // Task not found
        }
    }

    public boolean updateTask(String id, String title, String description, String status, LocalDate dueDate) {
        try {
            int rowsAffected = jdbcTemplate.update(UPDATE_TASK, title, description, status, dueDate, id);
            return rowsAffected > 0; // Returns true if task was found and updated
        } catch (Exception e) {
            return false; // Update failed
        }
    }

    public boolean deleteTask(String id) {
        try {
            int rowsAffected = jdbcTemplate.update(SOFT_DELETE_TASK, id);
            return rowsAffected > 0; // Returns true if task was found and deleted
        } catch (Exception e) {
            return false;
        }
    }

    public long countAllTasks() {
        return jdbcTemplate.queryForObject(COUNT_TASKS, Long.class);
    }

    public List<TaskModel> selectAllTasksPaginated(int limit, int offset) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(SELECT_ALL_TASKS_PAGINATED, limit, offset);

        List<TaskModel> tasks = new ArrayList<>();

        for (Map<String, Object> result : results) {
            TaskModel task = new TaskModel();
            task.setId((String) result.get("id"));
            task.setTitle((String) result.get("title"));
            task.setDescription((String) result.get("description"));
            task.setStatus(TaskStatus.valueOf((String) result.get("status")));

            // Handle date conversion properly
            Object dueDateObj = result.get("due_date");
            if (dueDateObj != null) {
                if (dueDateObj instanceof java.sql.Date) {
                    task.setDueDate(((java.sql.Date) dueDateObj).toLocalDate());
                } else if (dueDateObj instanceof java.time.LocalDate) {
                    task.setDueDate((LocalDate) dueDateObj);
                }
            }

            tasks.add(task);
        }
        return tasks;
    }
}
