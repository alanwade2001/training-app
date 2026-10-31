package com.acme.wo.internal.data;

import com.acme.wo.model.ExecutionState;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DataJpaTest
class SessionPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndRetrievesCompleteSession() {
        Instant startedAt =
                Instant.parse("2026-10-30T10:00:00Z");

        Instant updatedAt =
                Instant.parse("2026-10-30T10:05:00Z");

        Instant stateStartedAt =
                Instant.parse("2026-10-30T10:04:00Z");

        ExecutionState state = ExecutionState.values()[0];

        SessionExecutionEntity execution =
                SessionExecutionEntity.builder()
                        .exerciseIndex(1)
                        .currentSet(2)
                        .stateStartedAt(stateStartedAt)
                        .state(state)
                        .build();

        SessionEntity session =
                SessionEntity.builder()
                        .workoutId(10L)
                        .workoutName("Speed Endurance")
                        .startedAt(startedAt)
                        .updatedAt(updatedAt)
                        .currentSessionExecution(execution)
                        .build();

        SessionExerciseEntity exercise1 =
                SessionExerciseEntity.builder()
                        .sequence(1)
                        .name("300m")
                        .sets(2)
                        .reps(1)
                        .recoveryInSecs(180)
                        .session(session)
                        .build();

        SessionExerciseEntity exercise2 =
                SessionExerciseEntity.builder()
                        .sequence(2)
                        .name("150m")
                        .sets(2)
                        .reps(1)
                        .recoveryInSecs(120)
                        .session(session)
                        .build();

        session.setExercises(
                List.of(exercise1, exercise2)
        );

        entityManager.persist(session);
        entityManager.flush();

        Long sessionId = session.getId();

        entityManager.clear();

        SessionEntity retrieved =
                entityManager.find(
                        SessionEntity.class,
                        sessionId
                );

        assertNotNull(retrieved);

        assertEquals(10L, retrieved.getWorkoutId());

        assertEquals(
                "Speed Endurance",
                retrieved.getWorkoutName()
        );

        assertEquals(
                startedAt,
                retrieved.getStartedAt()
        );

        assertEquals(
                updatedAt,
                retrieved.getUpdatedAt()
        );

        SessionExecutionEntity retrievedExecution =
                retrieved.getCurrentSessionExecution();

        assertNotNull(retrievedExecution);

        assertEquals(
                1,
                retrievedExecution.getExerciseIndex()
        );

        assertEquals(
                2,
                retrievedExecution.getCurrentSet()
        );

        assertEquals(
                stateStartedAt,
                retrievedExecution.getStateStartedAt()
        );

        assertEquals(
                state,
                retrievedExecution.getState()
        );

        assertEquals(
                2,
                retrieved.getExercises().size()
        );

        SessionExerciseEntity retrievedExercise1 =
                findExercise(retrieved, 1);

        assertEquals(
                "300m",
                retrievedExercise1.getName()
        );

        assertEquals(
                2,
                retrievedExercise1.getSets()
        );

        assertEquals(
                1,
                retrievedExercise1.getReps()
        );

        assertEquals(
                180,
                retrievedExercise1.getRecoveryInSecs()
        );

        assertSame(
                retrieved,
                retrievedExercise1.getSession()
        );

        SessionExerciseEntity retrievedExercise2 =
                findExercise(retrieved, 2);

        assertEquals(
                "150m",
                retrievedExercise2.getName()
        );

        assertEquals(
                120,
                retrievedExercise2.getRecoveryInSecs()
        );

        assertSame(
                retrieved,
                retrievedExercise2.getSession()
        );
    }

    @Test
    void preservesSessionSnapshotWhenSourceWorkoutChanges() {
        WorkoutEntity workout =
                WorkoutEntity.builder()
                        .name("Original Workout")
                        .build();

        entityManager.persist(workout);
        entityManager.flush();

        SessionEntity session =
                sessionSnapshot(
                        workout.getId(),
                        "Original Workout",
                        "Original Exercise"
                );

        entityManager.persist(session);
        entityManager.flush();

        Long sessionId = session.getId();
        Long workoutId = workout.getId();

        entityManager.clear();

        WorkoutEntity storedWorkout =
                entityManager.find(
                        WorkoutEntity.class,
                        workoutId
                );

        storedWorkout.setName("Changed Workout");

        entityManager.flush();
        entityManager.clear();

        SessionEntity retrievedSession =
                entityManager.find(
                        SessionEntity.class,
                        sessionId
                );

        assertNotNull(retrievedSession);

        assertEquals(
                workoutId,
                retrievedSession.getWorkoutId()
        );

        assertEquals(
                "Original Workout",
                retrievedSession.getWorkoutName()
        );

        assertEquals(
                "Original Exercise",
                retrievedSession
                        .getExercises()
                        .getFirst()
                        .getName()
        );
    }

    private SessionEntity sessionSnapshot(
            Long workoutId,
            String workoutName,
            String exerciseName
    ) {
        ExecutionState state = ExecutionState.values()[0];

        SessionEntity session =
                SessionEntity.builder()
                        .workoutId(workoutId)
                        .workoutName(workoutName)
                        .startedAt(
                                Instant.parse(
                                        "2026-10-30T10:00:00Z"
                                )
                        )
                        .updatedAt(
                                Instant.parse(
                                        "2026-10-30T10:05:00Z"
                                )
                        )
                        .currentSessionExecution(
                                SessionExecutionEntity.builder()
                                        .exerciseIndex(0)
                                        .currentSet(1)
                                        .stateStartedAt(
                                                Instant.parse(
                                                        "2026-10-30T10:01:00Z"
                                                )
                                        )
                                        .state(state)
                                        .build()
                        )
                        .build();

        SessionExerciseEntity exercise =
                SessionExerciseEntity.builder()
                        .sequence(1)
                        .name(exerciseName)
                        .sets(3)
                        .reps(1)
                        .recoveryInSecs(180)
                        .session(session)
                        .build();

        session.setExercises(List.of(exercise));

        return session;
    }

    private SessionExerciseEntity findExercise(
            SessionEntity session,
            int sequence
    ) {
        return session.getExercises()
                .stream()
                .filter(exercise ->
                        exercise.getSequence() == sequence)
                .findFirst()
                .orElseThrow();
    }
}
