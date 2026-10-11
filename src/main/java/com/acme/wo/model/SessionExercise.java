package com.acme.wo.model;

public record SessionExercise(
        int sequence,
        String name,
        int sets,
        int reps,
        int recoveryInSecs) {
}
