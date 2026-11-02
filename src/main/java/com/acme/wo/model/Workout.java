package com.acme.wo.model;

import java.time.DayOfWeek;
import java.util.List;

public record Workout(
        Long id,
        String name,
        DayOfWeek dayOfWeek,
        List<WorkoutExercise> exercises
) {
}