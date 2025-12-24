package com.example.taskmanagerapp.exceptions;

import com.example.taskmanagerapp.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDate(InvalidDateException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_DATE", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidStatusException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_STATUS", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidTitleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTitle(InvalidTitleException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_TITLE", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(TaskNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("TASK_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(404).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Something went wrong");
        return ResponseEntity.status(500).body(error);
    }

    @ExceptionHandler(InvalidPageOrSizeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPageOrSize(InvalidPageOrSizeException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_PAGE_OR_SIZE", ex.getMessage());
        return ResponseEntity.status(400).body(error);
    }
}
