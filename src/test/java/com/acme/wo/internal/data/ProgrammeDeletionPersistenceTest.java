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
class ProgrammeDeletionPersistenceTest extends PostgresqlIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void deletingProgrammeDeletesCompleteOwnedHierarchy() {
        ProgrammeEntity programme =
                createProgramme(
                        "400m Programme",
                        1,
                        "Speed Endurance",
                        "400m"
                );

        entityManager.persist(programme);
        entityManager.flush();

        WeekEntity week = programme.getWeeks().getFirst();
        WorkoutEntity workout = week.getWorkouts().getFirst();
        WorkoutExerciseEntity exercise =
                workout.getExercises().getFirst();

        Long programmeId = programme.getId();
        Long weekId = week.getId();
        Long workoutId = workout.getId();
        Long exerciseId = exercise.getId();

        entityManager.remove(programme);
        entityManager.flush();
        entityManager.clear();

        assertNull(
                entityManager.find(
                        ProgrammeEntity.class,
                        programmeId
                )
        );

        assertNull(
                entityManager.find(
                        WeekEntity.class,
                        weekId
                )
        );

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
    void deletingProgrammePreservesHistoricalSession() {
        ProgrammeEntity programme =
                createProgramme(
                        "400m Programme",
                        1,
                        "Speed Endurance",
                        "400m"
                );

        entityManager.persist(programme);
        entityManager.flush();

        WorkoutEntity workout = programme
                .getWeeks()
                .getFirst()
                .getWorkouts()
                .getFirst();

        SessionEntity session =
                createSessionSnapshot(
                        workout.getId(),
                        workout.getName(),
                        workout.getExercises().getFirst()
                );

        entityManager.persist(session);
        entityManager.flush();

        Long programmeId = programme.getId();
        Long workoutId = workout.getId();
        Long sessionId = session.getId();

        entityManager.remove(programme);
        entityManager.flush();
        entityManager.clear();

        assertNull(
                entityManager.find(
                        ProgrammeEntity.class,
                        programmeId
                )
        );

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

        assertEquals(1, snapshot.getSequence());
        assertEquals("400m", snapshot.getName());
        assertEquals(3, snapshot.getSets());
        assertEquals(1, snapshot.getReps());
        assertEquals(180, snapshot.getRecoveryInSecs());
    }

    @Test
    void deletingOneProgrammeDoesNotAffectAnotherProgramme() {
        ProgrammeEntity programmeA =
                createProgramme(
                        "Programme A",
                        1,
                        "Workout A",
                        "Exercise A"
                );

        ProgrammeEntity programmeB =
                createProgramme(
                        "Programme B",
                        1,
                        "Workout B",
                        "Exercise B"
                );

        entityManager.persist(programmeA);
        entityManager.persist(programmeB);
        entityManager.flush();

        Long programmeAId = programmeA.getId();

        Long weekAId =
                programmeA.getWeeks().getFirst().getId();

        Long workoutAId = programmeA
                .getWeeks()
                .getFirst()
                .getWorkouts()
                .getFirst()
                .getId();

        Long exerciseAId = programmeA
                .getWeeks()
                .getFirst()
                .getWorkouts()
                .getFirst()
                .getExercises()
                .getFirst()
                .getId();

        Long programmeBId = programmeB.getId();

        Long weekBId =
                programmeB.getWeeks().getFirst().getId();

        Long workoutBId = programmeB
                .getWeeks()
                .getFirst()
                .getWorkouts()
                .getFirst()
                .getId();

        Long exerciseBId = programmeB
                .getWeeks()
                .getFirst()
                .getWorkouts()
                .getFirst()
                .getExercises()
                .getFirst()
                .getId();

        entityManager.remove(programmeA);
        entityManager.flush();
        entityManager.clear();

        assertNull(
                entityManager.find(
                        ProgrammeEntity.class,
                        programmeAId
                )
        );

        assertNull(
                entityManager.find(
                        WeekEntity.class,
                        weekAId
                )
        );

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

        ProgrammeEntity retrievedProgrammeB =
                entityManager.find(
                        ProgrammeEntity.class,
                        programmeBId
                );

        assertNotNull(retrievedProgrammeB);

        assertNotNull(
                entityManager.find(
                        WeekEntity.class,
                        weekBId
                )
        );

        assertNotNull(
                entityManager.find(
                        WorkoutEntity.class,
                        workoutBId
                )
        );

        assertNotNull(
                entityManager.find(
                        WorkoutExerciseEntity.class,
                        exerciseBId
                )
        );

        assertEquals(
                "Programme B",
                retrievedProgrammeB.getName()
        );

        assertEquals(
                "Workout B",
                retrievedProgrammeB
                        .getWeeks()
                        .getFirst()
                        .getWorkouts()
                        .getFirst()
                        .getName()
        );

        assertEquals(
                "Exercise B",
                retrievedProgrammeB
                        .getWeeks()
                        .getFirst()
                        .getWorkouts()
                        .getFirst()
                        .getExercises()
                        .getFirst()
                        .getName()
        );
    }

    private ProgrammeEntity createProgramme(
            String programmeName,
            int weekNumber,
            String workoutName,
            String exerciseName
    ) {
        ProgrammeEntity programme =
                ProgrammeEntity.builder()
                        .name(programmeName)
                        .build();

        WeekEntity week =
                WeekEntity.builder()
                        .number(weekNumber)
                        .programme(programme)
                        .build();

        WorkoutEntity workout =
                WorkoutEntity.builder()
                        .name(workoutName)
                        .plannedDay(DayOfWeek.THURSDAY)
                        .week(week)
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
        week.setWorkouts(List.of(workout));
        programme.setWeeks(List.of(week));

        return programme;
    }

    private SessionEntity createSessionSnapshot(
            Long workoutId,
            String workoutName,
            WorkoutExerciseEntity sourceExercise
    ) {
        Instant startedAt =
                Instant.parse("2026-10-30T10:00:00Z");

        SessionEntity session =
                SessionEntity.builder()
                        .workoutId(workoutId)
                        .workoutName(workoutName)
                        .startedAt(startedAt)
                        .updatedAt(startedAt)
                        .build();

        SessionExerciseEntity snapshot =
                SessionExerciseEntity.builder()
                        .sequence(sourceExercise.getSequence())
                        .name(sourceExercise.getName())
                        .sets(sourceExercise.getSets())
                        .reps(sourceExercise.getReps())
                        .recoveryInSecs(
                                sourceExercise.getRecoveryInSecs()
                        )
                        .session(session)
                        .build();

        session.setExercises(List.of(snapshot));

        return session;
    }
}
