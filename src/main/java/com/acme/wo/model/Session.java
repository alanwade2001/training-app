package com.acme.wo.model;

import java.util.List;

public record Session(
        Long id,
        String workoutId,
        String workoutName,
        List<SessionExercise> exercises) {
}
