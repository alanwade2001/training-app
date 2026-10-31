package com.acme.wo.model.upload;

import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import static com.acme.wo.model.upload.TestData.*;
import static com.acme.wo.model.upload.UploadAssertions.*;

class WorkoutUploadValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @Test
    void validWorkoutHasNoValidationErrors() {
        WorkoutUpload workout = TestData.validWorkout();

        Set<ConstraintViolation<WorkoutUpload>> violations =
                validator.validate(workout);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nameMustNotBeNull() {
        WorkoutUpload workout = new WorkoutUpload(
                null,
                DayOfWeek.MONDAY,
                List.of(validExercise())
        );

        var violations = validator.validate(workout);

        assertViolationOn(violations, "name");
    }

    @Test
    void nameMustNotBeBlank() {
        WorkoutUpload workout = new WorkoutUpload(
                "   ",
                DayOfWeek.MONDAY,
                List.of(validExercise())
        );

        var violations = validator.validate(workout);

        assertViolationOn(violations, "name");
    }

    @Test
    void plannedDayMustNotBeNull() {
        WorkoutUpload workout = new WorkoutUpload(
                "Track Session",
                null,
                List.of(validExercise())
        );

        var violations = validator.validate(workout);

        assertViolationOn(violations, "plannedDay");
    }

    @Test
    void exercisesMustNotBeEmpty() {
        WorkoutUpload workout = new WorkoutUpload(
                "Track Session",
                DayOfWeek.MONDAY,
                List.of()
        );

        var violations = validator.validate(workout);

        assertViolationOn(violations, "exercises");
    }

    @Test
    void exercisesMustNotBeNull() {
        WorkoutUpload workout = new WorkoutUpload(
                "Track Session",
                DayOfWeek.MONDAY,
                null
        );

        var violations = validator.validate(workout);

        assertViolationOn(violations, "exercises");
    }

    @Test
    void invalidExerciseMakesWorkoutInvalid() {
        WorkoutUpload workout = invalidWorkout();

        Set<ConstraintViolation<WorkoutUpload>> violations =
                validator.validate(workout);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .startsWith("exercises[0]"))
        );
    }


}