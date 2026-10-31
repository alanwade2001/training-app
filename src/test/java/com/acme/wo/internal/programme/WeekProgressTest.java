package com.acme.wo.internal.programme;

import com.acme.wo.model.Week;
import com.acme.wo.model.Workout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class WeekProgressTest {

    @ParameterizedTest(
            name = "{index}: total={0}, completed={1} -> done={2}"
    )
    @MethodSource("progressScenarios")
    void calculatesWeekProgress(
            int totalWorkouts,
            long completedWorkouts,
            boolean expectedDone
    ) {
        Week week = weekWithWorkouts(totalWorkouts);

        WeekProgress progress =
                new WeekProgress(
                        week,
                        completedWorkouts
                );

        assertEquals(
                totalWorkouts,
                progress.totalWorkouts()
        );

        assertEquals(
                completedWorkouts,
                progress.completedWorkouts()
        );

        assertEquals(
                expectedDone,
                progress.done()
        );
    }

    private static Stream<Arguments> progressScenarios() {
        return Stream.of(
                // Empty week is not done.
                Arguments.of(0, 0L, false),

                // No completed workouts.
                Arguments.of(3, 0L, false),

                // Partially complete week.
                Arguments.of(3, 1L, false),
                Arguments.of(3, 2L, false),

                // Every planned workout complete.
                Arguments.of(3, 3L, true),

                // Single-workout week.
                Arguments.of(1, 0L, false),
                Arguments.of(1, 1L, true)
        );
    }

    private static Week weekWithWorkouts(
            int workoutCount
    ) {
        List<Workout> workouts =
                IntStream.range(0, workoutCount)
                        .mapToObj(i -> mock(Workout.class))
                        .toList();

        return mockWeek(workouts);
    }

    private static Week mockWeek(
            List<Workout> workouts
    ) {
        Week week = mock(Week.class);

        org.mockito.Mockito
                .when(week.workouts())
                .thenReturn(workouts);

        return week;
    }
}