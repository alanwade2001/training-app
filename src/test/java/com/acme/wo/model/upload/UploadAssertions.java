package com.acme.wo.model.upload;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UploadAssertions {
    static void assertViolationOn(
            Set<ConstraintViolation<WorkoutUpload>> violations,
            String property) {

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals(property)),
                "Expected validation violation on " + property
        );
    }
}
