package com.acme.wo.model.upload;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeekUploadTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @Test
    void validWeekHasNoViolations() {
        WeekUpload week = TestData.validWeek();

        Set<ConstraintViolation<WeekUpload>> violations =
                validator.validate(week);

        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsNonPositiveNumber() {
        WeekUpload valid = TestData.validWeek();

        WeekUpload week = new WeekUpload(
                0,
                valid.workouts()
        );

        Set<ConstraintViolation<WeekUpload>> violations =
                validator.validate(week);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("number"))
        );
    }

    @Test
    void rejectsEmptyWorkouts() {
        WeekUpload valid = TestData.validWeek();

        WeekUpload week = new WeekUpload(
                valid.number(),
                List.of()
        );

        Set<ConstraintViolation<WeekUpload>> violations =
                validator.validate(week);

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
        WeekUpload week = new WeekUpload(
                1,
                List.of(TestData.invalidWorkout())
        );

        Set<ConstraintViolation<WeekUpload>> violations =
                validator.validate(week);

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