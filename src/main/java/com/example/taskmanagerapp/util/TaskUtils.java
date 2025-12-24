package com.example.taskmanagerapp.util;

import com.example.taskmanagerapp.enums.TaskStatus;

public class TaskUtils {
    public static  boolean isValidTaskStatus(String status) {
        try {
            TaskStatus.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
