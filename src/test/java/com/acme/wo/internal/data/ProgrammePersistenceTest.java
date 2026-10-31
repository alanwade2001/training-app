package com.acme.wo.internal.data;

import com.acme.wo.model.ExerciseUnit;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DataJpaTest
class ProgrammePersistenceTest extends PostgresqlIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndRetrievesCompleteProgrammeHierarchy() {
        ProgrammeEntity programme = ProgrammeEntity.builder()
                .name("400m Programme")
                .build();

        WeekEntity week1 = WeekEntity.builder()
                .number(1)
                .programme(programme)
                .build();

        WeekEntity week2 = WeekEntity.builder()
                .number(2)
                .programme(programme)
                .build();

        WorkoutEntity speedWorkout = WorkoutEntity.builder()
                .name("Speed")
                .plannedDay(DayOfWeek.TUESDAY)
                .week(week1)
                .build();

        WorkoutEntity speedEnduranceWorkout =
                WorkoutEntity.builder()
                        .name("Speed Endurance")
                        .plannedDay(DayOfWeek.THURSDAY)
                        .week(week2)
                        .build();

        WorkoutExerciseEntity exercise1 =
                WorkoutExerciseEntity.builder()
                        .sequence(1)
                        .name("200m")
                        .sets(2)
                        .reps(3)
                        .quantity(200)
                        .units(ExerciseUnit.METERS)
                        .recoveryInSecs(120)
                        .workout(speedWorkout)
                        .build();

        WorkoutExerciseEntity exercise2 =
                WorkoutExerciseEntity.builder()
                        .sequence(2)
                        .name("100m")
                        .sets(2)
                        .reps(2)
                        .quantity(100)
                        .units(ExerciseUnit.METERS)
                        .recoveryInSecs(90)
                        .workout(speedWorkout)
                        .build();

        WorkoutExerciseEntity exercise3 =
                WorkoutExerciseEntity.builder()
                        .sequence(1)
                        .name("400m")
                        .sets(3)
                        .reps(1)
                        .quantity(400)
                        .units(ExerciseUnit.METERS)
                        .recoveryInSecs(180)
                        .workout(speedEnduranceWorkout)
                        .build();

        speedWorkout.setExercises(
                List.of(exercise1, exercise2)
        );

        speedEnduranceWorkout.setExercises(
                List.of(exercise3)
        );

        week1.setWorkouts(
                List.of(speedWorkout)
        );

        week2.setWorkouts(
                List.of(speedEnduranceWorkout)
        );

        programme.setWeeks(
                List.of(week1, week2)
        );

        /*
         * Only the aggregate root is persisted.
         *
         * ProgrammeEntity cascades to WeekEntity,
         * WeekEntity cascades to WorkoutEntity,
         * and WorkoutEntity should cascade to
         * WorkoutExerciseEntity.
         */
        entityManager.persist(programme);
        entityManager.flush();

        Long programmeId = programme.getId();

        entityManager.clear();

        ProgrammeEntity retrieved =
                entityManager.find(
                        ProgrammeEntity.class,
                        programmeId
                );

        assertNotNull(retrieved);
        assertEquals(
                "400m Programme",
                retrieved.getName()
        );

        assertEquals(2, retrieved.getWeeks().size());

        WeekEntity retrievedWeek1 =
                findWeek(retrieved, 1);

        WeekEntity retrievedWeek2 =
                findWeek(retrieved, 2);

        assertEquals(1, retrievedWeek1.getNumber());
        assertEquals(2, retrievedWeek2.getNumber());

        assertSame(
                retrieved,
                retrievedWeek1.getProgramme()
        );

        assertSame(
                retrieved,
                retrievedWeek2.getProgramme()
        );

        assertEquals(
                1,
                retrievedWeek1.getWorkouts().size()
        );

        assertEquals(
                1,
                retrievedWeek2.getWorkouts().size()
        );

        WorkoutEntity retrievedSpeed =
                retrievedWeek1.getWorkouts().getFirst();

        assertEquals(
                "Speed",
                retrievedSpeed.getName()
        );

        assertEquals(
                DayOfWeek.TUESDAY,
                retrievedSpeed.getPlannedDay()
        );

        assertSame(
                retrievedWeek1,
                retrievedSpeed.getWeek()
        );

        assertEquals(
                2,
                retrievedSpeed.getExercises().size()
        );

        WorkoutExerciseEntity retrieved200m =
                findExercise(retrievedSpeed, 1);

        WorkoutExerciseEntity retrieved100m =
                findExercise(retrievedSpeed, 2);

        assertEquals("200m", retrieved200m.getName());
        assertEquals(2, retrieved200m.getSets());
        assertEquals(3, retrieved200m.getReps());
        assertEquals(200, retrieved200m.getQuantity());
        assertEquals(ExerciseUnit.METERS, retrieved200m.getUnits());
        assertEquals(
                120,
                retrieved200m.getRecoveryInSecs()
        );

        assertSame(
                retrievedSpeed,
                retrieved200m.getWorkout()
        );

        assertEquals("100m", retrieved100m.getName());
        assertEquals(2, retrieved100m.getSequence());

        assertSame(
                retrievedSpeed,
                retrieved100m.getWorkout()
        );

        WorkoutEntity retrievedSpeedEndurance =
                retrievedWeek2.getWorkouts().getFirst();

        assertEquals(
                "Speed Endurance",
                retrievedSpeedEndurance.getName()
        );

        assertEquals(
                DayOfWeek.THURSDAY,
                retrievedSpeedEndurance.getPlannedDay()
        );

        assertSame(
                retrievedWeek2,
                retrievedSpeedEndurance.getWeek()
        );

        assertEquals(
                1,
                retrievedSpeedEndurance
                        .getExercises()
                        .size()
        );

        WorkoutExerciseEntity retrieved400m =
                retrievedSpeedEndurance
                        .getExercises()
                        .getFirst();

        assertEquals("400m", retrieved400m.getName());
        assertEquals(400, retrieved400m.getQuantity());
        assertEquals(
                180,
                retrieved400m.getRecoveryInSecs()
        );
    }

    @Test
    void persistsMultipleProgrammesIndependently() {
        ProgrammeEntity programmeA =
                programme(
                        "Programme A",
                        1,
                        "Workout A"
                );

        ProgrammeEntity programmeB =
                programme(
                        "Programme B",
                        1,
                        "Workout B"
                );

        entityManager.persist(programmeA);
        entityManager.persist(programmeB);
        entityManager.flush();

        Long programmeAId = programmeA.getId();
        Long programmeBId = programmeB.getId();

        entityManager.clear();

        ProgrammeEntity retrievedA =
                entityManager.find(
                        ProgrammeEntity.class,
                        programmeAId
                );

        ProgrammeEntity retrievedB =
                entityManager.find(
                        ProgrammeEntity.class,
                        programmeBId
                );

        assertNotNull(retrievedA);
        assertNotNull(retrievedB);

        assertEquals(
                "Programme A",
                retrievedA.getName()
        );

        assertEquals(
                "Programme B",
                retrievedB.getName()
        );

        assertEquals(
                "Workout A",
                retrievedA
                        .getWeeks()
                        .getFirst()
                        .getWorkouts()
                        .getFirst()
                        .getName()
        );

        assertEquals(
                "Workout B",
                retrievedB
                        .getWeeks()
                        .getFirst()
                        .getWorkouts()
                        .getFirst()
                        .getName()
        );
    }

    private ProgrammeEntity programme(
            String programmeName,
            int weekNumber,
            String workoutName
    ) {
        ProgrammeEntity programme =
                ProgrammeEntity.builder()
                        .name(programmeName)
                        .build();

        WeekEntity week = WeekEntity.builder()
                .number(weekNumber)
                .programme(programme)
                .build();

        WorkoutEntity workout =
                WorkoutEntity.builder()
                        .name(workoutName)
                        .plannedDay(DayOfWeek.MONDAY)
                        .week(week)
                        .build();

        WorkoutExerciseEntity exercise =
                WorkoutExerciseEntity.builder()
                        .sequence(1)
                        .name("Exercise")
                        .sets(1)
                        .reps(1)
                        .quantity(100)
                        .units(ExerciseUnit.METERS)
                        .recoveryInSecs(60)
                        .workout(workout)
                        .build();

        workout.setExercises(List.of(exercise));
        week.setWorkouts(List.of(workout));
        programme.setWeeks(List.of(week));

        return programme;
    }

    private WeekEntity findWeek(
            ProgrammeEntity programme,
            int number
    ) {
        return programme.getWeeks()
                .stream()
                .filter(week -> week.getNumber() == number)
                .findFirst()
                .orElseThrow();
    }

    private WorkoutExerciseEntity findExercise(
            WorkoutEntity workout,
            int sequence
    ) {
        return workout.getExercises()
                .stream()
                .filter(exercise ->
                        exercise.getSequence() == sequence)
                .findFirst()
                .orElseThrow();
    }
}