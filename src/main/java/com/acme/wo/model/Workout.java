package com.acme.wo.model;

import java.util.List;

public record Workout(
        String id,
        String name,
        List<WorkoutExercise> exercises
) {
}