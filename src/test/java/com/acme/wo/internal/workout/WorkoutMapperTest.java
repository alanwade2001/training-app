package com.acme.wo.internal.workout;

import com.acme.wo.internal.data.WorkoutEntity;
import com.acme.wo.internal.data.WorkoutExerciseEntity;
import com.acme.wo.model.ExerciseUnit;
import com.acme.wo.model.Workout;
import com.acme.wo.model.WorkoutExercise;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class WorkoutMapperTest {

    @Test
    void mapsWorkoutToEntity() {
        var exercise = new WorkoutExercise(
                1,
                "400m",
                3,
                2,
                400,
                ExerciseUnit.METERS,
                180
        );

        var workout = new Workout(
                1L,
                "Speed Endurance",
                DayOfWeek.THURSDAY,
                List.of(exercise)
        );

        WorkoutEntity entity = WorkoutMapper.toEntity(workout);

        assertEquals("Speed Endurance", entity.getName());
        assertEquals(DayOfWeek.THURSDAY, entity.getPlannedDay());

        assertEquals(1, entity.getExercises().size());

        WorkoutExerciseEntity mappedExercise =
                entity.getExercises().getFirst();

        assertEquals(1, mappedExercise.getSequence());
        assertEquals("400m", mappedExercise.getName());
        assertEquals(3, mappedExercise.getSets());
        assertEquals(2, mappedExercise.getReps());
        assertEquals(400, mappedExercise.getQuantity());
        assertEquals(ExerciseUnit.METERS, mappedExercise.getUnits());
        assertEquals(180, mappedExercise.getRecoveryInSecs());

        assertSame(entity, mappedExercise.getWorkout());
    }

    @Test
    void mapsEntityToWorkout() {
        var entity = WorkoutEntity.builder()
                .name("Speed Endurance")
                .plannedDay(DayOfWeek.THURSDAY)
                .build();

        var exerciseEntity = WorkoutExerciseEntity.builder()
                .sequence(1)
                .name("400m")
                .sets(3)
                .reps(2)
                .quantity(400)
                .units(ExerciseUnit.KG)
                .recoveryInSecs(180)
                .workout(entity)
                .build();

        entity.setExercises(List.of(exerciseEntity));

        Workout workout = WorkoutMapper.fromEntity(entity);

        assertEquals("Speed Endurance", workout.name());
        assertEquals(DayOfWeek.THURSDAY, workout.dayOfWeek());

        assertEquals(1, workout.exercises().size());

        WorkoutExercise exercise = workout.exercises().getFirst();

        assertEquals(1, exercise.sequence());
        assertEquals("400m", exercise.name());
        assertEquals(3, exercise.sets());
        assertEquals(2, exercise.reps());
        assertEquals(400, exercise.quantity());
        assertEquals(ExerciseUnit.KG, exercise.units());
        assertEquals(180, exercise.recoveryInSecs());
    }
}