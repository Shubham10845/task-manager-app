package com.example.taskmanagerapp.dto;

import com.example.taskmanagerapp.models.TaskModel;

import java.util.List;

public class PaginatedTaskResponseDTO {
    private List<TaskResponseDTO> tasks;
    private boolean hasMore;
    private long total;
    private int page;
    private int size;

    public PaginatedTaskResponseDTO(List<TaskResponseDTO> tasks, boolean hasMore, long total, int page, int size) {
        this.tasks = tasks;
        this.hasMore = hasMore;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<TaskResponseDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskResponseDTO> tasks) {
        this.tasks = tasks;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
