package com.example.taskmanagerapp.exceptions;

public class InvalidPageOrSizeException extends RuntimeException {
    public InvalidPageOrSizeException(String message) {
        super(message);
    }
}
