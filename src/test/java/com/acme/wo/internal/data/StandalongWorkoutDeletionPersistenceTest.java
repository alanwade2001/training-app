package com.acme.wo.internal.data;

import com.acme.wo.model.ExerciseUnit;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
class StandaloneWorkoutDeletionPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void deletingStandaloneWorkoutDeletesOwnedExercises() {
        WorkoutEntity workout =
                createStandaloneWorkout(
                        "Speed Endurance",
                        "400m"
                );

        entityManager.persist(workout);
        entityManager.flush();

        Long workoutId = workout.getId();
        Long exerciseId =
                workout.getExercises().getFirst().getId();

        entityManager.remove(workout);
        entityManager.flush();
        entityManager.clear();

        assertNull(
                entityManager.find(
                        WorkoutEntity.class,
                        workoutId
                )
        );

        assertNull(
                entityManager.find(
                        WorkoutExerciseEntity.class,
                        exerciseId
                )
        );
    }

    @Test
    void deletingStandaloneWorkoutPreservesHistoricalSession() {
        WorkoutEntity workout =
                createStandaloneWorkout(
                        "Speed Endurance",
                        "400m"
                );

        entityManager.persist(workout);
        entityManager.flush();

        WorkoutExerciseEntity workoutExercise =
                workout.getExercises().getFirst();

        SessionEntity session =
                createSessionSnapshot(
                        workout.getId(),
                        workout.getName(),
                        workoutExercise
                );

        entityManager.persist(session);
        entityManager.flush();

        Long workoutId = workout.getId();
        Long sessionId = session.getId();
        Long sessionExerciseId =
                session.getExercises().getFirst().getId();

        entityManager.remove(workout);
        entityManager.flush();
        entityManager.clear();

        assertNull(
                entityManager.find(
                        WorkoutEntity.class,
                        workoutId
                )
        );

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
                "Speed Endurance",
                retrievedSession.getWorkoutName()
        );

        assertEquals(
                1,
                retrievedSession.getExercises().size()
        );

        SessionExerciseEntity snapshot =
                retrievedSession.getExercises().getFirst();

        assertEquals(
                sessionExerciseId,
                snapshot.getId()
        );

        assertEquals(1, snapshot.getSequence());
        assertEquals("400m", snapshot.getName());
        assertEquals(3, snapshot.getSets());
        assertEquals(1, snapshot.getReps());

        assertEquals(
                180,
                snapshot.getRecoveryInSecs()
        );

        assertNotNull(
                entityManager.find(
                        SessionExerciseEntity.class,
                        sessionExerciseId
                )
        );
    }

    @Test
    void deletingOneStandaloneWorkoutDoesNotAffectAnother() {
        WorkoutEntity workoutA =
                createStandaloneWorkout(
                        "Workout A",
                        "Exercise A"
                );

        WorkoutEntity workoutB =
                createStandaloneWorkout(
                        "Workout B",
                        "Exercise B"
                );

        entityManager.persist(workoutA);
        entityManager.persist(workoutB);
        entityManager.flush();

        Long workoutAId = workoutA.getId();
        Long exerciseAId =
                workoutA.getExercises().getFirst().getId();

        Long workoutBId = workoutB.getId();
        Long exerciseBId =
                workoutB.getExercises().getFirst().getId();

        entityManager.remove(workoutA);
        entityManager.flush();
        entityManager.clear();

        assertNull(
                entityManager.find(
                        WorkoutEntity.class,
                        workoutAId
                )
        );

        assertNull(
                entityManager.find(
                        WorkoutExerciseEntity.class,
                        exerciseAId
                )
        );

        WorkoutEntity retrievedWorkoutB =
                entityManager.find(
                        WorkoutEntity.class,
                        workoutBId
                );

        assertNotNull(retrievedWorkoutB);

        assertEquals(
                "Workout B",
                retrievedWorkoutB.getName()
        );

        assertNull(retrievedWorkoutB.getWeek());

        assertEquals(
                1,
                retrievedWorkoutB.getExercises().size()
        );

        WorkoutExerciseEntity retrievedExerciseB =
                entityManager.find(
                        WorkoutExerciseEntity.class,
                        exerciseBId
                );

        assertNotNull(retrievedExerciseB);

        assertEquals(
                "Exercise B",
                retrievedExerciseB.getName()
        );

        assertEquals(
                workoutBId,
                retrievedExerciseB
                        .getWorkout()
                        .getId()
        );
    }

    private WorkoutEntity createStandaloneWorkout(
            String workoutName,
            String exerciseName
    ) {
        WorkoutEntity workout =
                WorkoutEntity.builder()
                        .name(workoutName)
                        .plannedDay(DayOfWeek.THURSDAY)
                        .week(null)
                        .build();

        WorkoutExerciseEntity exercise =
                WorkoutExerciseEntity.builder()
                        .sequence(1)
                        .name(exerciseName)
                        .sets(3)
                        .reps(1)
                        .quantity(400)
                        .units(ExerciseUnit.METERS)
                        .recoveryInSecs(180)
                        .workout(workout)
                        .build();

        workout.setExercises(List.of(exercise));

        return workout;
    }

    private SessionEntity createSessionSnapshot(
            Long workoutId,
            String workoutName,
            WorkoutExerciseEntity sourceExercise
    ) {
        Instant startedAt =
                Instant.parse("2026-10-30T10:00:00Z");

        Instant updatedAt =
                Instant.parse("2026-10-30T10:05:00Z");

        SessionEntity session =
                SessionEntity.builder()
                        .workoutId(workoutId)
                        .workoutName(workoutName)
                        .startedAt(startedAt)
                        .updatedAt(updatedAt)
                        .build();

        SessionExerciseEntity snapshot =
                SessionExerciseEntity.builder()
                        .sequence(
                                sourceExercise.getSequence()
                        )
                        .name(
                                sourceExercise.getName()
                        )
                        .sets(
                                sourceExercise.getSets()
                        )
                        .reps(
                                sourceExercise.getReps()
                        )
                        .recoveryInSecs(
                                sourceExercise
                                        .getRecoveryInSecs()
                        )
                        .session(session)
                        .build();

        session.setExercises(List.of(snapshot));

        return session;
    }
}
