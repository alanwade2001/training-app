package com.acme.wo.model;

public record WorkoutExercise(
        int sequence,
        String name,
        int sets,
        int reps,
        int recoveryInSecs
){}
