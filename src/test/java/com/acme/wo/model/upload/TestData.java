package com.acme.wo.model.upload;

import com.acme.wo.model.ExerciseUnit;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestData {


    static WorkoutUpload createWorkout(String name, DayOfWeek day, List<@Valid WorkoutExerciseUpload> exercises) {
        return new WorkoutUpload(
                name,
                day,
                exercises
        );
    }

    static WeekUpload validWeek() {
        return new WeekUpload(1, List.of(validWorkout()));
    }

    static WeekUpload invalidWeek() {
        return new WeekUpload(1, List.of(invalidWorkout()));
    }

    static WorkoutUpload validWorkout() {
        return new WorkoutUpload(
                "Track Session",
                DayOfWeek.MONDAY,
                List.of(validExercise())
        );
    }

    static WorkoutUpload invalidWorkout() {
        return new WorkoutUpload(
                "Track Session",
                DayOfWeek.MONDAY,
                List.of(invalidExercise())
        );
    }

    static WorkoutExerciseUpload validExercise() {
        return new WorkoutExerciseUpload("exercise1", 1, 1, 0, ExerciseUnit.KG, 30);
    }

    static WorkoutExerciseUpload invalidExercise() {
        return new WorkoutExerciseUpload("exercise1", 1, 1, 0, ExerciseUnit.KG, 0);
    }
}
