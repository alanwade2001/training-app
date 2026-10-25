package com.acme.wo.internal.workout;

import com.acme.wo.internal.data.WorkoutEntity;
import com.acme.wo.internal.data.WorkoutExerciseEntity;
import com.acme.wo.model.Workout;
import com.acme.wo.model.WorkoutExercise;
import org.springframework.stereotype.Component;

public class WorkoutMapper {

    public static WorkoutEntity toEntity(Workout w) {
        var workoutEntity = WorkoutEntity.builder()
                .name(w.name())
                .plannedDay(w.dayOfWeek())
                .build();

        var exerciseEntities = w.exercises().stream().map(exercise ->
                WorkoutExerciseEntity.builder()
                        .name(exercise.name())
                        .recoveryInSecs(exercise.recoveryInSecs())
                        .reps(exercise.reps())
                        .sequence(exercise.sequence())
                        .sets(exercise.sets())
                        .quantity(exercise.quantity())
                        .units(exercise.exerciseUnit())
                        .workout(workoutEntity)
                        .build()
        ).toList();

        workoutEntity.setExercises(exerciseEntities);

        return workoutEntity;
    }

    public static Workout fromEntity(WorkoutEntity workoutEntity) {
        var exercises = workoutEntity.getExercises().stream().map(exercise ->
                        new WorkoutExercise(exercise.getSequence()
                                , exercise.getName()
                                , exercise.getSets()
                                , exercise.getReps()
                                , exercise.getQuantity()
                                , exercise.getUnits()
                                , exercise.getRecoveryInSecs())
                )
                .toList();

        return new Workout(workoutEntity.getId(), workoutEntity.getName(), workoutEntity.getPlannedDay(), exercises);
    }
}
