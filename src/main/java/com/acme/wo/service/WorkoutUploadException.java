package com.acme.wo.service;

public class WorkoutUploadException extends RuntimeException {
    public WorkoutUploadException(String message) {
        super(message);
    }
    public WorkoutUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
