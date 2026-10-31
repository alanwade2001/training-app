package com.acme.wo.internal.workout;

import com.acme.wo.internal.data.WorkoutEntity;
import com.acme.wo.internal.data.WorkoutExerciseEntity;
import com.acme.wo.model.ExerciseUnit;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WorkoutPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndRetrievesStandaloneWorkout() {

        var workout = WorkoutEntity.builder()
                .name("Speed Endurance")
                .plannedDay(DayOfWeek.THURSDAY)
                .build();

        var exercise1 = WorkoutExerciseEntity.builder()
                .sequence(1)
                .name("300m")
                .sets(2)
                .reps(1)
                .quantity(300)
                .units(ExerciseUnit.METERS)
                .recoveryInSecs(180)
                .workout(workout)
                .build();

        var exercise2 = WorkoutExerciseEntity.builder()
                .sequence(2)
                .name("150m")
                .sets(2)
                .reps(1)
                .quantity(150)
                .units(ExerciseUnit.METERS)
                .recoveryInSecs(120)
                .workout(workout)
                .build();

        workout.setExercises(
                List.of(exercise1, exercise2)
        );

        entityManager.persist(workout);
        entityManager.flush();

        Long workoutId = workout.getId();

        /*
         * Critical to the test.
         *
         * Without clear(), find() could return the entity already
         * held in Hibernate's persistence context rather than
         * reconstructing it from the database.
         */
        entityManager.clear();

        WorkoutEntity retrieved =
                entityManager.find(
                        WorkoutEntity.class,
                        workoutId
                );

        assertNotNull(retrieved);

        assertEquals(
                "Speed Endurance",
                retrieved.getName()
        );

        assertEquals(
                DayOfWeek.THURSDAY,
                retrieved.getPlannedDay()
        );

        assertEquals(
                2,
                retrieved.getExercises().size()
        );

        var retrievedExercise1 =
                retrieved.getExercises().getFirst();

        assertEquals(1, retrievedExercise1.getSequence());
        assertEquals("300m", retrievedExercise1.getName());
        assertEquals(2, retrievedExercise1.getSets());
        assertEquals(1, retrievedExercise1.getReps());
        assertEquals(300, retrievedExercise1.getQuantity());
        assertEquals(ExerciseUnit.METERS, retrievedExercise1.getUnits());
        assertEquals(
                180,
                retrievedExercise1.getRecoveryInSecs()
        );

        var retrievedExercise2 =
                retrieved.getExercises().get(1);

        assertEquals(2, retrievedExercise2.getSequence());
        assertEquals("150m", retrievedExercise2.getName());
        assertEquals(2, retrievedExercise2.getSets());
        assertEquals(1, retrievedExercise2.getReps());
        assertEquals(150, retrievedExercise2.getQuantity());
        assertEquals(ExerciseUnit.METERS, retrievedExercise2.getUnits());
        assertEquals(
                120,
                retrievedExercise2.getRecoveryInSecs()
        );
    }
}