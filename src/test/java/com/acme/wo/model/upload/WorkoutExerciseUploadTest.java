package com.acme.wo.model.upload;

import static org.junit.jupiter.api.Assertions.*;

import com.acme.wo.model.ExerciseUnit;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkoutExerciseUploadTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @Test
    void validWorkoutExercisePassesValidation() {
        WorkoutExerciseUpload exercise =
                new WorkoutExerciseUpload(
                        "Running",
                        4,
                        5,
                        200,
                        ExerciseUnit.METERS,
                        60
                );

        Set<ConstraintViolation<WorkoutExerciseUpload>> violations =
                validator.validate(exercise);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nameMustNotBeNull() {
        WorkoutExerciseUpload exercise =
                validExercise(null, 4, 5, 200, ExerciseUnit.METERS, 60);

        assertHasViolation(exercise, "name");
    }

    @Test
    void nameMustNotBeBlank() {
        WorkoutExerciseUpload exercise =
                validExercise("   ", 4, 5, 200, ExerciseUnit.METERS, 60);

        assertHasViolation(exercise, "name");
    }

    @Test
    void setsMustBePositive() {
        WorkoutExerciseUpload exercise =
                validExercise("Running", 0, 5, 200, ExerciseUnit.METERS, 60);

        assertHasViolation(exercise, "sets");
    }

    @Test
    void setsMustNotBeNegative() {
        WorkoutExerciseUpload exercise =
                validExercise("Running", -1, 5, 200, ExerciseUnit.METERS, 60);

        assertHasViolation(exercise, "sets");
    }

    @Test
    void repsMustBePositive() {
        WorkoutExerciseUpload exercise =
                validExercise("Running", 4, 0, 200, ExerciseUnit.METERS, 60);

        assertHasViolation(exercise, "reps");
    }

    @Test
    void repsMustNotBeNegative() {
        WorkoutExerciseUpload exercise =
                validExercise("Running", 4, -1, 200, ExerciseUnit.METERS, 60);

        assertHasViolation(exercise, "reps");
    }

    @Test
    void quantityMayBeZero() {
        WorkoutExerciseUpload exercise =
                validExercise("Running", 4, 5, 0, ExerciseUnit.METERS, 60);

        assertNoViolation(exercise, "quantity");
    }

    @Test
    void quantityMustNotBeNegative() {
        WorkoutExerciseUpload exercise =
                validExercise("Running", 4, 5, -1, ExerciseUnit.METERS, 60);

        assertHasViolation(exercise, "quantity");
    }

    @Test
    void unitsMustNotBeNull() {
        WorkoutExerciseUpload exercise =
                validExercise("Running", 4, 5, 200, null, 60);

        assertHasViolation(exercise, "units");
    }

    @Test
    void recoveryMayBeExactlyFifteenSeconds() {
        WorkoutExerciseUpload exercise =
                validExercise("Running", 4, 5, 200, ExerciseUnit.METERS, 15);

        assertNoViolation(exercise, "recoveryInSecs");
    }

    @Test
    void recoveryMustNotBeLessThanFifteenSeconds() {
        WorkoutExerciseUpload exercise =
                validExercise("Running", 4, 5, 200, ExerciseUnit.METERS, 14);

        assertHasViolation(exercise, "recoveryInSecs");
    }

    private WorkoutExerciseUpload validExercise(
            String name,
            int sets,
            int reps,
            int quantity,
            ExerciseUnit units,
            int recoveryInSecs
    ) {
        return new WorkoutExerciseUpload(
                name,
                sets,
                reps,
                quantity,
                units,
                recoveryInSecs
        );
    }

    private void assertHasViolation(
            WorkoutExerciseUpload exercise,
            String property
    ) {
        Set<ConstraintViolation<WorkoutExerciseUpload>> violations =
                validator.validate(exercise);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath()
                                        .toString()
                                        .equals(property)),
                "Expected validation error for property: " + property
        );
    }

    private void assertNoViolation(
            WorkoutExerciseUpload exercise,
            String property
    ) {
        Set<ConstraintViolation<WorkoutExerciseUpload>> violations =
                validator.validate(exercise);

        assertFalse(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath()
                                        .toString()
                                        .equals(property)),
                "Did not expect validation error for property: " + property
        );
    }
}