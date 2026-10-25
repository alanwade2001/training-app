package com.acme.wo.model;

import java.time.Instant;
import java.util.List;

public record Session(
        Long id,
        Long workoutId,
        String workoutName,
        Instant startedAt,
        Instant updatedAt,
        SessionExecution execution,
        List<SessionExercise> exercises) {

    public boolean hasMoreExercises(int currentSet) {
        return exercises.size() > currentSet;
    }

    public SessionExercise getCurrentExercise() {
        return exercises.get(execution.exerciseIndex());
    }
}
