package com.acme.wo.internal.programme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkoutProgressTest {

    @ParameterizedTest(name =
            "{0} completed sessions -> done = {1}")
    @CsvSource({
            "0, false",
            "1, true",
            "2, true",
            "5, true"
    })
    void workoutIsDoneWhenAtLeastOneSessionIsCompleted(
            long completedSessions,
            boolean expectedDone
    ) {
        boolean done = isDone(completedSessions);

        assertEquals(expectedDone, done);
    }

    private boolean isDone(long completedSessions) {
        return completedSessions > 0;
    }
}