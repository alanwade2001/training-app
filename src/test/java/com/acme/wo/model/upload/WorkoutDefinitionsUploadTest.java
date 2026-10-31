package com.acme.wo.model.upload;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorkoutDefinitionsUploadTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @Test
    void validDefinitionHasNoViolations() {
        var def = new WorkoutDefinitionsUpload(
                List.of(TestData.validWorkout())
        );

        var violations = validator.validate(def);

        assertTrue(violations.isEmpty());
    }



    @Test
    void rejectsEmptyWorkouts() {


        WorkoutDefinitionsUpload def = new WorkoutDefinitionsUpload(
                List.of()
        );

        var violations = validator.validate(def);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("workouts"))
        );
    }

    @Test
    void validatesWorkout() {
        var def = new WorkoutDefinitionsUpload(
                List.of(TestData.invalidWorkout())
        );

        var violations = validator.validate(def);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .startsWith("workouts[0]"))
        );
    }

}