package com.acme.wo.model.upload;

import static org.junit.jupiter.api.Assertions.*;

import com.acme.wo.model.ExerciseUnit;
import jakarta.validation.*;
import jakarta.validation.constraints.NotEmpty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgrammeUploadTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @Test
    void rejectsBlankName() {
        ProgrammeUpload programme =
                new ProgrammeUpload(
                        "",
                        List.of(validWeek())
                );

        Set<ConstraintViolation<ProgrammeUpload>> violations =
                validator.validate(programme);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("name"))
        );
    }

    @Test
    void rejectsEmptyWeeks() {
        ProgrammeUpload programme =
                new ProgrammeUpload(
                        "400m Programme",
                        List.of()
                );

        Set<ConstraintViolation<ProgrammeUpload>> violations =
                validator.validate(programme);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("weeks"))
        );
    }

    @Test
    void validProgrammeHasNoViolations() {
        ProgrammeUpload programme =
                new ProgrammeUpload(
                        "400m Programme",
                        List.of(validWeek())
                );

        Set<ConstraintViolation<ProgrammeUpload>> violations =
                validator.validate(programme);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "   "})
    void rejectsInvalidName(String name) {
        ProgrammeUpload programme =
                new ProgrammeUpload(
                        name,
                        List.of(validWeek())
                );

        Set<ConstraintViolation<ProgrammeUpload>> violations =
                validator.validate(programme);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("name"))
        );
    }

    private WeekUpload validWeek() {
        WeekUpload week = new WeekUpload(1, validWorkouts());
        return week;
    }

    private @NotEmpty List<@Valid WorkoutUpload> validWorkouts() {
        WorkoutUpload workout = new WorkoutUpload("w1", DayOfWeek.FRIDAY, validWorkoutExercise());
        return List.of(workout);
    }

    private @NotEmpty List<@Valid WorkoutExerciseUpload> validWorkoutExercise() {
        WorkoutExerciseUpload exercise = new WorkoutExerciseUpload("ex1", 2, 1, 0, ExerciseUnit.KG, 90);
        return List.of(exercise);
    }
}